package com.dhc.inspection_system.auth;

public class JwtResponse {

    private String token;
    private String message;

    public JwtResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public String getToken() { return token; }
    public String getMessage() { return message; }
}