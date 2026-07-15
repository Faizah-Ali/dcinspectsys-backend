package com.dhc.inspection_system.controller;

import com.dhc.inspection_system.auth.JwtResponse;
import com.dhc.inspection_system.dto.LoginRequest;
import com.dhc.inspection_system.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public JwtResponse login(@RequestBody LoginRequest request) {
        return loginService.login(request);
    }
}
