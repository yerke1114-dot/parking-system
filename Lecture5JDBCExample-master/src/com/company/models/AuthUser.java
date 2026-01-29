package com.company.models;

public class AuthUser {
    private final int UserId;
    private final String username;
    private final String role;

    public AuthUser(int userId, String username, String role) {
        this.UserId = userId;
        this.username = username;
        this.role = role;
    }

    public int getUserId() {
        return UserId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}