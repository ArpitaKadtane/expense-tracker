package com.diligent.expense.dto;

import java.util.List;

public record GroupDetailResponse(
        Long id,
        String name,
        String ownerUsername,
        List<MemberResponse> members
) {
}
