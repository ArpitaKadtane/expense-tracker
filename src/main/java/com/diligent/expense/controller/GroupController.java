package com.diligent.expense.controller;

import com.diligent.expense.dto.AddMemberRequest;
import com.diligent.expense.dto.BalanceResponse;
import com.diligent.expense.dto.CreateGroupExpenseRequest;
import com.diligent.expense.dto.CreateGroupRequest;
import com.diligent.expense.dto.GroupDetailResponse;
import com.diligent.expense.dto.GroupExpenseResponse;
import com.diligent.expense.dto.GroupSummaryResponse;
import com.diligent.expense.dto.MemberResponse;
import com.diligent.expense.dto.SettlementResponse;
import com.diligent.expense.service.GroupService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupSummaryResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(groupService.createGroup(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<GroupSummaryResponse>> listGroups(Authentication authentication) {
        return ResponseEntity.ok(groupService.listGroups(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDetailResponse> getGroup(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(groupService.getGroup(id, authentication.getName()));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<MemberResponse> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(groupService.addMember(id, request, authentication.getName()));
    }

    @PostMapping("/{id}/expenses")
    public ResponseEntity<GroupExpenseResponse> addExpense(
            @PathVariable Long id,
            @Valid @RequestBody CreateGroupExpenseRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(groupService.addExpense(id, request, authentication.getName()));
    }

    @GetMapping("/{id}/expenses")
    public ResponseEntity<List<GroupExpenseResponse>> listExpenses(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(groupService.listExpenses(id, authentication.getName()));
    }

    @GetMapping("/{id}/balances")
    public ResponseEntity<List<BalanceResponse>> getBalances(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(groupService.getBalances(id, authentication.getName()));
    }

    @GetMapping("/{id}/settle-up")
    public ResponseEntity<List<SettlementResponse>> getSettleUp(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(groupService.getSettleUp(id, authentication.getName()));
    }
}
