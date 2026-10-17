package com.diligent.expense.dto;

import java.time.LocalDateTime;

public record GroupSummaryResponse(
        Long id,
        String name,
        String ownerUsername,
        long memberCount,
        LocalDateTime createdAt
) {
}
