package com.diligent.expense.service;

/**
 * Thrown when group-expense split inputs are invalid (bad totals, percentages
 * that do not sum to 100, duplicate participants, ...). Mapped to HTTP 400.
 */
public class InvalidSplitException extends RuntimeException {

    public InvalidSplitException(String message) {
        super(message);
    }
}
