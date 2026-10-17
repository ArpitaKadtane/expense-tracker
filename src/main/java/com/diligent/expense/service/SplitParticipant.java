package com.diligent.expense.service;

import java.math.BigDecimal;

/**
 * One participant of a split. {@code value} carries the exact amount for an
 * EXACT split or the percentage for a PERCENTAGE split; it is ignored for an
 * EQUAL split.
 */
public record SplitParticipant(String username, BigDecimal value) {
}
