package com.dhc.inspection_system.controller;

import com.dhc.inspection_system.dto.LoginRequest;
import com.dhc.inspection_system.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dhc.inspection_system.auth.JwtResponse;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Object login(@RequestBody LoginRequest request) {

        String result = loginService.login(request);

        // Error cases → return JSON
        if ("USER_NOT_FOUND".equals(result)) {
            return new JwtResponse(null, "USER_NOT_FOUND");
        }

        if ("INVALID_PASSWORD".equals(result)) {
            return new JwtResponse(null, "INVALID_PASSWORD");
        }

        // Success → return token
        return new JwtResponse(result, "Login Successful");
    }
}