package com.dhc.inspection_system.service;

import com.dhc.inspection_system.auth.JwtResponse;
import com.dhc.inspection_system.dto.LoginRequest;

public interface LoginService {
    JwtResponse login(LoginRequest request);
}
