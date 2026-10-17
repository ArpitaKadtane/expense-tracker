package com.diligent.expense.dto;

import java.math.BigDecimal;

/**
 * A member's position in a group: how much they paid, how much they owe, and
 * the net (positive means the group owes them).
 */
public record BalanceResponse(
        String username,
        BigDecimal paid,
        BigDecimal owed,
        BigDecimal net
) {
}
