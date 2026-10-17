package com.diligent.expense.dto;

import jakarta.validation.constraints.NotBlank;

public record AddMemberRequest(
        @NotBlank(message = "Username is required")
        String username
) {
}
