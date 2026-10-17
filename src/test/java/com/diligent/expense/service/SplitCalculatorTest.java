package com.diligent.expense.service;

import com.diligent.expense.model.SplitType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SplitCalculatorTest {

    private final SplitCalculator calculator = new SplitCalculator();

    private static SplitParticipant participant(String username) {
        return new SplitParticipant(username, null);
    }

    private static SplitParticipant participant(String username, String value) {
        return new SplitParticipant(username, new BigDecimal(value));
    }

    private static BigDecimal sum(Map<String, BigDecimal> shares) {
        return shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ------------------------------------------------------------------
    // Equal
    // ------------------------------------------------------------------

    @Test
    void splitsEquallyWhenDivisible() {
        Map<String, BigDecimal> shares = calculator.split(
                new BigDecimal("10.00"), SplitType.EQUAL,
                List.of(participant("a"), participant("b")));

        assertEquals(new BigDecimal("5.00"), shares.get("a"));
        assertEquals(new BigDecimal("5.00"), shares.get("b"));
    }

    @Test
    void distributesLeftoverCentsOnEqualSplit() {
        Map<String, BigDecimal> shares = calculator.split(
                new BigDecimal("10.00"), SplitType.EQUAL,
                List.of(participant("a"), participant("b"), participant("c")));

        assertEquals(new BigDecimal("3.34"), shares.get("a"));
        assertEquals(new BigDecimal("3.33"), shares.get("b"));
        assertEquals(new BigDecimal("3.33"), shares.get("c"));
        assertEquals(new BigDecimal("10.00"), sum(shares));
    }

    // ------------------------------------------------------------------
    // Exact
    // ------------------------------------------------------------------

    @Test
    void exactSplitKeepsProvidedAmounts() {
        Map<String, BigDecimal> shares = calculator.split(
                new BigDecimal("30.00"), SplitType.EXACT,
                List.of(participant("a", "10.00"), participant("b", "20.00")));

        assertEquals(new BigDecimal("10.00"), shares.get("a"));
        assertEquals(new BigDecimal("20.00"), shares.get("b"));
    }

    @Test
    void exactSplitRejectsAmountsThatDoNotAddUp() {
        assertThrows(InvalidSplitException.class, () -> calculator.split(
                new BigDecimal("30.00"), SplitType.EXACT,
                List.of(participant("a", "10.00"), participant("b", "10.00"))));
    }

    // ------------------------------------------------------------------
    // Percentage
    // ------------------------------------------------------------------

    @Test
    void percentageSplitComputesAmounts() {
        Map<String, BigDecimal> shares = calculator.split(
                new BigDecimal("200.00"), SplitType.PERCENTAGE,
                List.of(participant("a", "75"), participant("b", "25")));

        assertEquals(new BigDecimal("150.00"), shares.get("a"));
        assertEquals(new BigDecimal("50.00"), shares.get("b"));
    }

    @Test
    void percentageSplitAlwaysSumsToTotal() {
        Map<String, BigDecimal> shares = calculator.split(
                new BigDecimal("10.00"), SplitType.PERCENTAGE,
                List.of(participant("a", "33.33"),
                        participant("b", "33.33"),
                        participant("c", "33.34")));

        assertEquals(new BigDecimal("10.00"), sum(shares));
    }

    @Test
    void percentageSplitRejectsPercentagesThatDoNotSumTo100() {
        assertThrows(InvalidSplitException.class, () -> calculator.split(
                new BigDecimal("100.00"), SplitType.PERCENTAGE,
                List.of(participant("a", "50"), participant("b", "40"))));
    }

    // ------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------

    @Test
    void rejectsDuplicateParticipants() {
        assertThrows(InvalidSplitException.class, () -> calculator.split(
                new BigDecimal("10.00"), SplitType.EQUAL,
                List.of(participant("a"), participant("a"))));
    }

    @Test
    void rejectsEmptyParticipants() {
        assertThrows(InvalidSplitException.class, () -> calculator.split(
                new BigDecimal("10.00"), SplitType.EQUAL, List.of()));
    }

    @Test
    void rejectsNonPositiveTotal() {
        assertThrows(InvalidSplitException.class, () -> calculator.split(
                new BigDecimal("0.00"), SplitType.EQUAL,
                List.of(participant("a"), participant("b"))));
    }
}
