package com.diligent.expense.dto;

import java.math.BigDecimal;

public record ShareResponse(
        String username,
        BigDecimal amount
) {
}
