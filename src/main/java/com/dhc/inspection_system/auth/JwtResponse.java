package com.dhc.inspection_system.auth;

public class JwtResponse {

    private String token;
    private String username;
    private String role;
    private String group;
    private String message;

    public JwtResponse(String token, String username, String role, String group, String message) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.group = group;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getGroup() {
        return group;
    }

    public String getMessage() {
        return message;
    }
}
