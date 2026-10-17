package com.diligent.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GroupExpenseResponse(
        Long id,
        String title,
        BigDecimal amount,
        String category,
        LocalDate date,
        String paidBy,
        List<ShareResponse> shares
) {
}
