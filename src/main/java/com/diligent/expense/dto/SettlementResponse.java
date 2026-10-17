package com.diligent.expense.dto;

import java.math.BigDecimal;

/**
 * A suggested payment of {@code amount} from {@code from} to {@code to}.
 */
public record SettlementResponse(
        String from,
        String to,
        BigDecimal amount
) {
}
