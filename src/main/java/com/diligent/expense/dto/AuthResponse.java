package com.diligent.expense.dto;

public record AuthResponse(String token, String username, String tokenType) {

    public static AuthResponse bearer(String token, String username) {
        return new AuthResponse(token, username, "Bearer");
    }
}
