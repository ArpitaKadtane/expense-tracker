package com.diligent.expense.exception;

/**
 * Raised for a valid-but-illegal group operation, e.g. adding a non-member as a
 * participant of an expense. Mapped to HTTP 400.
 */
public class InvalidGroupOperationException extends RuntimeException {

    public InvalidGroupOperationException(String message) {
        super(message);
    }
}
