package com.diligent.expense.exception;

/**
 * Raised when a group does not exist, or when the caller is not a member of it.
 * Deliberately the same error for both cases so membership cannot be probed.
 */
public class GroupNotFoundException extends RuntimeException {

    public GroupNotFoundException(Long groupId) {
        super("Group with id " + groupId + " not found");
    }
}
