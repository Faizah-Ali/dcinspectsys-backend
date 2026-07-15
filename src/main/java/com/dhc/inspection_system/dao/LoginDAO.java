package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.LoginUserDTO;

public interface LoginDAO {
    LoginUserDTO getUserByUsername(String username);
}
