package com.diligent.expense.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Given each member's net balance (paid minus owed), produces a small set of
 * transfers that settles every debt.
 *
 * <p>Uses the classic greedy <em>minimum cash flow</em> heuristic: repeatedly
 * match the largest creditor with the largest debtor. This always settles the
 * group in at most {@code n - 1} transfers for {@code n} members.
 */
@Component
public class SettleUpCalculator {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.005");

    /** A single suggested payment of {@code amount} from {@code from} to {@code to}. */
    public record Settlement(String from, String to, BigDecimal amount) {
    }

    /**
     * @param netBalances username to net balance; positive means the group owes
     *                    them, negative means they owe the group.
     * @return the transfers required to settle up (empty when already settled).
     */
    public List<Settlement> settle(Map<String, BigDecimal> netBalances) {
        List<Party> creditors = new ArrayList<>();
        List<Party> debtors = new ArrayList<>();

        if (netBalances != null) {
            netBalances.forEach((username, net) -> {
                if (username == null || net == null) {
                    return;
                }
                BigDecimal rounded = net.setScale(2, RoundingMode.HALF_UP);
                if (rounded.signum() > 0) {
                    creditors.add(new Party(username, rounded));
                } else if (rounded.signum() < 0) {
                    debtors.add(new Party(username, rounded.negate()));
                }
            });
        }

        creditors.sort(Comparator.comparing(Party::amount).reversed());
        debtors.sort(Comparator.comparing(Party::amount).reversed());

        List<Settlement> settlements = new ArrayList<>();
        int creditorIndex = 0;
        int debtorIndex = 0;

        while (creditorIndex < creditors.size() && debtorIndex < debtors.size()) {
            Party creditor = creditors.get(creditorIndex);
            Party debtor = debtors.get(debtorIndex);

            BigDecimal payment = creditor.amount.min(debtor.amount);
            if (payment.signum() > 0) {
                settlements.add(new Settlement(
                        debtor.username,
                        creditor.username,
                        payment.setScale(2, RoundingMode.HALF_UP)));
            }

            creditor.amount = creditor.amount.subtract(payment);
            debtor.amount = debtor.amount.subtract(payment);

            if (creditor.amount.abs().compareTo(TOLERANCE) < 0) {
                creditorIndex++;
            }
            if (debtor.amount.abs().compareTo(TOLERANCE) < 0) {
                debtorIndex++;
            }
        }
        return settlements;
    }

    private static final class Party {
        private final String username;
        private BigDecimal amount;

        private Party(String username, BigDecimal amount) {
            this.username = username;
            this.amount = amount;
        }

        private String username() {
            return username;
        }

        private BigDecimal amount() {
            return amount;
        }
    }
}
