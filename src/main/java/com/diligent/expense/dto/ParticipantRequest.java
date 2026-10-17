package com.diligent.expense.dto;

import java.math.BigDecimal;

/**
 * A participant of a split. {@code value} is the exact amount for an EXACT
 * split or the percentage for a PERCENTAGE split; ignored for EQUAL.
 */
public record ParticipantRequest(
        String username,
        BigDecimal value
) {
}
