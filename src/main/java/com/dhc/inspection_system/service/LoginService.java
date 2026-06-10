package com.dhc.inspection_system.service;

import com.dhc.inspection_system.dto.LoginRequest;

public interface LoginService {
    String login(LoginRequest request);
}