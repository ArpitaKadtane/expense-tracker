package com.diligent.expense.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettleUpCalculatorTest {

    private final SettleUpCalculator calculator = new SettleUpCalculator();

    private static Map<String, BigDecimal> balances(String... usernameAmountPairs) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (int i = 0; i < usernameAmountPairs.length; i += 2) {
            map.put(usernameAmountPairs[i], new BigDecimal(usernameAmountPairs[i + 1]));
        }
        return map;
    }

    @Test
    void settlesSimpleTwoPersonDebt() {
        List<SettleUpCalculator.Settlement> settlements =
                calculator.settle(balances("alice", "20.00", "bob", "-20.00"));

        assertEquals(1, settlements.size());
        assertEquals("bob", settlements.get(0).from());
        assertEquals("alice", settlements.get(0).to());
        assertEquals(new BigDecimal("20.00"), settlements.get(0).amount());
    }

    @Test
    void returnsNothingWhenAlreadySettled() {
        assertTrue(calculator.settle(
                balances("alice", "0.00", "bob", "0.00")).isEmpty());
    }

    @Test
    void ignoresSubCentResiduals() {
        assertTrue(calculator.settle(
                balances("alice", "0.004", "bob", "-0.004")).isEmpty());
    }

    @Test
    void settlesThreeFriendsWithMinimumTransfers() {
        List<SettleUpCalculator.Settlement> settlements =
                calculator.settle(balances(
                        "alice", "40.00",
                        "bob", "20.00",
                        "carol", "-30.00",
                        "dave", "-30.00"));

        assertEquals(3, settlements.size());
        assertEquals("carol", settlements.get(0).from());
        assertEquals("alice", settlements.get(0).to());
        assertEquals(new BigDecimal("30.00"), settlements.get(0).amount());

        assertEquals("dave", settlements.get(1).from());
        assertEquals("alice", settlements.get(1).to());
        assertEquals(new BigDecimal("10.00"), settlements.get(1).amount());

        assertEquals("dave", settlements.get(2).from());
        assertEquals("bob", settlements.get(2).to());
        assertEquals(new BigDecimal("20.00"), settlements.get(2).amount());
    }

    @Test
    void settlesToZeroWithManyParticipants() {
        Map<String, BigDecimal> nets = balances(
                "a", "15.00",
                "b", "10.00",
                "c", "-5.00",
                "d", "-10.00",
                "e", "-10.00");

        List<SettleUpCalculator.Settlement> settlements = calculator.settle(nets);

        // Apply the settlements back onto the balances; everyone should reach zero.
        Map<String, BigDecimal> applied = new LinkedHashMap<>(nets);
        for (SettleUpCalculator.Settlement settlement : settlements) {
            applied.merge(settlement.from(), settlement.amount(), BigDecimal::add);
            applied.merge(settlement.to(), settlement.amount().negate(), BigDecimal::add);
        }
        applied.values().forEach(remaining ->
                assertEquals(0, remaining.compareTo(BigDecimal.ZERO),
                        "expected settled balance, got " + remaining));
    }
}
