package com.diligent.expense.service;

import com.diligent.expense.dto.AddMemberRequest;
import com.diligent.expense.dto.BalanceResponse;
import com.diligent.expense.dto.CreateGroupExpenseRequest;
import com.diligent.expense.dto.CreateGroupRequest;
import com.diligent.expense.dto.GroupDetailResponse;
import com.diligent.expense.dto.GroupExpenseResponse;
import com.diligent.expense.dto.GroupSummaryResponse;
import com.diligent.expense.dto.MemberResponse;
import com.diligent.expense.dto.ParticipantRequest;
import com.diligent.expense.dto.SettlementResponse;
import com.diligent.expense.dto.ShareResponse;

import com.diligent.expense.exception.GroupNotFoundException;
import com.diligent.expense.exception.InvalidGroupOperationException;
import com.diligent.expense.exception.UserNotFoundException;

import com.diligent.expense.model.ExpenseGroup;
import com.diligent.expense.model.ExpenseShare;
import com.diligent.expense.model.GroupExpense;
import com.diligent.expense.model.GroupMembership;
import com.diligent.expense.model.SplitType;
import com.diligent.expense.model.User;

import com.diligent.expense.repository.ExpenseShareRepository;
import com.diligent.expense.repository.GroupExpenseRepository;
import com.diligent.expense.repository.GroupMembershipRepository;
import com.diligent.expense.repository.GroupRepository;
import com.diligent.expense.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic for shared groups: membership, group expenses, balances and
 * the settle-up suggestions. Every read/write is scoped to the calling user's
 * membership, so a non-member always gets a 404.
 */
@Service
public class GroupService {

    private static final int SCALE = 2;

    private final GroupRepository groups;
    private final GroupMembershipRepository memberships;
    private final GroupExpenseRepository groupExpenses;
    private final ExpenseShareRepository expenseShares;
    private final UserRepository users;
    private final SplitCalculator splitCalculator;
    private final SettleUpCalculator settleUpCalculator;

    public GroupService(GroupRepository groups,
                        GroupMembershipRepository memberships,
                        GroupExpenseRepository groupExpenses,
                        ExpenseShareRepository expenseShares,
                        UserRepository users,
                        SplitCalculator splitCalculator,
                        SettleUpCalculator settleUpCalculator) {
        this.groups = groups;
        this.memberships = memberships;
        this.groupExpenses = groupExpenses;
        this.expenseShares = expenseShares;
        this.users = users;
        this.splitCalculator = splitCalculator;
        this.settleUpCalculator = settleUpCalculator;
    }

    // ------------------------------------------------------------------
    // Groups & members
    // ------------------------------------------------------------------

    @Transactional
    public GroupSummaryResponse createGroup(CreateGroupRequest request, String username) {
        User owner = requireUser(username);
        ExpenseGroup group = groups.save(new ExpenseGroup(request.name(), owner));
        addMembership(group, owner);

        if (request.memberUsernames() != null) {
            for (String memberUsername : request.memberUsernames()) {
                if (memberUsername == null || memberUsername.isBlank()
                        || memberUsername.equals(owner.getUsername())) {
                    continue;
                }
                addMembership(group, requireUser(memberUsername));
            }
        }
        return toSummary(group);
    }

    @Transactional(readOnly = true)
    public List<GroupSummaryResponse> listGroups(String username) {
        return groups.findAllForUser(username).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupDetailResponse getGroup(Long groupId, String username) {
        return toDetail(requireMember(groupId, username));
    }

    @Transactional
    public MemberResponse addMember(Long groupId, AddMemberRequest request, String username) {
        ExpenseGroup group = requireMember(groupId, username);
        User member = requireUser(request.username());
        addMembership(group, member);
        return new MemberResponse(member.getId(), member.getUsername());
    }

    // ------------------------------------------------------------------
    // Group expenses
    // ------------------------------------------------------------------

    @Transactional
    public GroupExpenseResponse addExpense(Long groupId,
                                           CreateGroupExpenseRequest request,
                                           String username) {
        requireMember(groupId, username);

        String payerUsername = (request.paidBy() == null || request.paidBy().isBlank())
                ? username
                : request.paidBy();
        User payer = requireUser(payerUsername);
        requireMembership(groupId, payer.getUsername());

        SplitType splitType = request.splitType() == null ? SplitType.EQUAL : request.splitType();

        List<SplitParticipant> participants = resolveParticipants(groupId, request);

        Map<String, BigDecimal> computedShares =
                splitCalculator.split(request.amount(), splitType, participants);

        GroupExpense expense = new GroupExpense();
        expense.setGroup(groups.getReferenceById(groupId));
        expense.setTitle(request.title());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setDate(request.date());
        expense.setPaidBy(payer);
        GroupExpense saved = groupExpenses.save(expense);

        List<ExpenseShare> savedShares = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : computedShares.entrySet()) {
            User participant = requireUser(entry.getKey());
            savedShares.add(expenseShares.save(
                    new ExpenseShare(saved, participant, entry.getValue())));
        }

        return toExpenseResponse(saved, savedShares);
    }

    @Transactional(readOnly = true)
    public List<GroupExpenseResponse> listExpenses(Long groupId, String username) {
        requireMember(groupId, username);
        return groupExpenses.findByGroupIdOrderByDateDescIdDesc(groupId).stream()
                .map(expense -> toExpenseResponse(
                        expense, expenseShares.findByGroupExpenseId(expense.getId())))
                .toList();
    }

    // ------------------------------------------------------------------
    // Balances & settle-up
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BalanceResponse> getBalances(Long groupId, String username) {
        requireMember(groupId, username);
        return computeBalances(groupId);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettleUp(Long groupId, String username) {
        requireMember(groupId, username);

        Map<String, BigDecimal> netBalances = new LinkedHashMap<>();
        for (BalanceResponse balance : computeBalances(groupId)) {
            netBalances.put(balance.username(), balance.net());
        }

        return settleUpCalculator.settle(netBalances).stream()
                .map(settlement -> new SettlementResponse(
                        settlement.from(), settlement.to(), settlement.amount()))
                .toList();
    }

    private List<BalanceResponse> computeBalances(Long groupId) {
        Map<String, BigDecimal> paid = toAmountMap(groupExpenses.sumPaidByUser(groupId));
        Map<String, BigDecimal> owed = toAmountMap(expenseShares.sumOwedByUser(groupId));

        List<BalanceResponse> balances = new ArrayList<>();
        for (GroupMembership membership : memberships.findByGroupIdOrderByUserUsernameAsc(groupId)) {
            String name = membership.getUser().getUsername();
            BigDecimal memberPaid = paid.getOrDefault(name, BigDecimal.ZERO).setScale(SCALE, RoundingMode.HALF_UP);
            BigDecimal memberOwed = owed.getOrDefault(name, BigDecimal.ZERO).setScale(SCALE, RoundingMode.HALF_UP);
            balances.add(new BalanceResponse(
                    name,
                    memberPaid,
                    memberOwed,
                    memberPaid.subtract(memberOwed).setScale(SCALE, RoundingMode.HALF_UP)));
        }
        return balances;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private List<SplitParticipant> resolveParticipants(Long groupId, CreateGroupExpenseRequest request) {
        if (request.participants() == null || request.participants().isEmpty()) {
            return memberships.findByGroupIdOrderByUserUsernameAsc(groupId).stream()
                    .map(membership -> new SplitParticipant(
                            membership.getUser().getUsername(), null))
                    .toList();
        }

        List<SplitParticipant> participants = new ArrayList<>();
        for (ParticipantRequest participant : request.participants()) {
            requireMembership(groupId, participant.username());
            participants.add(new SplitParticipant(participant.username(), participant.value()));
        }
        return participants;
    }

    private ExpenseGroup requireMember(Long groupId, String username) {
        ExpenseGroup group = groups.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        if (!memberships.existsByGroupIdAndUserUsername(groupId, username)) {
            throw new GroupNotFoundException(groupId);
        }
        return group;
    }

    private void requireMembership(Long groupId, String username) {
        if (username == null || !memberships.existsByGroupIdAndUserUsername(groupId, username)) {
            throw new InvalidGroupOperationException(
                    "User '" + username + "' is not a member of group " + groupId);
        }
    }

    private User requireUser(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private void addMembership(ExpenseGroup group, User user) {
        if (!memberships.existsByGroupIdAndUserUsername(group.getId(), user.getUsername())) {
            memberships.save(new GroupMembership(group, user));
        }
    }

    private Map<String, BigDecimal> toAmountMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] row : rows) {
            String name = (String) row[0];
            BigDecimal amount = row[1] == null
                    ? BigDecimal.ZERO
                    : new BigDecimal(row[1].toString());
            map.put(name, amount);
        }
        return map;
    }

    private GroupSummaryResponse toSummary(ExpenseGroup group) {
        return new GroupSummaryResponse(
                group.getId(),
                group.getName(),
                group.getOwner().getUsername(),
                memberships.countByGroupId(group.getId()),
                group.getCreatedAt());
    }

    private GroupDetailResponse toDetail(ExpenseGroup group) {
        List<MemberResponse> members = memberships
                .findByGroupIdOrderByUserUsernameAsc(group.getId()).stream()
                .map(membership -> new MemberResponse(
                        membership.getUser().getId(),
                        membership.getUser().getUsername()))
                .toList();

        return new GroupDetailResponse(
                group.getId(),
                group.getName(),
                group.getOwner().getUsername(),
                members);
    }

    private GroupExpenseResponse toExpenseResponse(GroupExpense expense, List<ExpenseShare> shares) {
        List<ShareResponse> shareResponses = shares.stream()
                .map(share -> new ShareResponse(
                        share.getUser().getUsername(),
                        share.getAmount()))
                .toList();

        return new GroupExpenseResponse(
                expense.getId(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate(),
                expense.getPaidBy().getUsername(),
                shareResponses);
    }
}
