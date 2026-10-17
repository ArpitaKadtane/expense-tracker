package com.diligent.expense.service;

import com.diligent.expense.model.SplitType;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Divides an expense into a per-user share. All arithmetic uses
 * {@link BigDecimal} at 2 decimal places, and the produced shares always add
 * up exactly to the total (leftover cents are distributed deterministically).
 */
@Component
public class SplitCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal CENT = new BigDecimal("0.01");

    public Map<String, BigDecimal> split(BigDecimal total,
                                         SplitType type,
                                         List<SplitParticipant> participants) {

        if (total == null || total.signum() <= 0) {
            throw new InvalidSplitException("Total amount must be greater than zero");
        }
        if (type == null) {
            throw new InvalidSplitException("Split type is required");
        }
        if (participants == null || participants.isEmpty()) {
            throw new InvalidSplitException("At least one participant is required");
        }
        ensureUnique(participants);

        BigDecimal normalizedTotal = total.setScale(SCALE, ROUNDING);

        return switch (type) {
            case EQUAL -> splitEqually(normalizedTotal, participants);
            case EXACT -> splitExactly(normalizedTotal, participants);
            case PERCENTAGE -> splitByPercentage(normalizedTotal, participants);
        };
    }

    private Map<String, BigDecimal> splitEqually(BigDecimal total, List<SplitParticipant> participants) {
        int count = participants.size();
        BigDecimal base = total.divide(BigDecimal.valueOf(count), SCALE, RoundingMode.DOWN);
        BigDecimal remainder = total.subtract(base.multiply(BigDecimal.valueOf(count)));
        int leftoverCents = remainder.movePointRight(SCALE).intValueExact();

        Map<String, BigDecimal> shares = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            BigDecimal share = i < leftoverCents ? base.add(CENT) : base;
            shares.put(participants.get(i).username(), share);
        }
        return shares;
    }

    private Map<String, BigDecimal> splitExactly(BigDecimal total, List<SplitParticipant> participants) {
        Map<String, BigDecimal> shares = new LinkedHashMap<>();
        BigDecimal sum = BigDecimal.ZERO;

        for (SplitParticipant participant : participants) {
            if (participant.value() == null || participant.value().signum() < 0) {
                throw new InvalidSplitException(
                        "Each participant needs a non-negative amount for an exact split");
            }
            BigDecimal amount = participant.value().setScale(SCALE, ROUNDING);
            shares.put(participant.username(), amount);
            sum = sum.add(amount);
        }

        if (sum.compareTo(total) != 0) {
            throw new InvalidSplitException(
                    "Exact split amounts must add up to the total (expected " + total + ", got " + sum + ")");
        }
        return shares;
    }

    private Map<String, BigDecimal> splitByPercentage(BigDecimal total, List<SplitParticipant> participants) {
        BigDecimal percentageSum = BigDecimal.ZERO;
        for (SplitParticipant participant : participants) {
            if (participant.value() == null || participant.value().signum() < 0) {
                throw new InvalidSplitException("Each participant needs a non-negative percentage");
            }
            percentageSum = percentageSum.add(participant.value());
        }

        if (percentageSum.compareTo(HUNDRED) != 0) {
            throw new InvalidSplitException("Percentages must add up to 100 (got " + percentageSum + ")");
        }

        Map<String, BigDecimal> shares = new LinkedHashMap<>();
        BigDecimal assigned = BigDecimal.ZERO;
        int lastIndex = participants.size() - 1;

        for (int i = 0; i < participants.size(); i++) {
            SplitParticipant participant = participants.get(i);
            BigDecimal amount;
            if (i == lastIndex) {
                amount = total.subtract(assigned);
            } else {
                amount = total.multiply(participant.value())
                        .divide(HUNDRED, SCALE, ROUNDING);
                assigned = assigned.add(amount);
            }
            shares.put(participant.username(), amount);
        }
        return shares;
    }

    private void ensureUnique(List<SplitParticipant> participants) {
        Set<String> seen = new HashSet<>();
        for (SplitParticipant participant : participants) {
            String username = participant.username();
            if (username == null || username.isBlank()) {
                throw new InvalidSplitException("Participant username is required");
            }
            if (!seen.add(username)) {
                throw new InvalidSplitException("Duplicate participant: " + username);
            }
        }
    }
}
