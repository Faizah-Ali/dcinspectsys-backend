package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.auth.JwtUtil;
import com.dhc.inspection_system.dao.LoginDAO;
import com.dhc.inspection_system.dto.LoginRequest;
import com.dhc.inspection_system.service.LoginService;
import com.dhc.inspection_system.utils.SHAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private LoginDAO loginDAO;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(LoginRequest request) {

        try {

            String dbPassword = loginDAO.getPasswordByUsername(request.getUsername());

            if (dbPassword == null) {
                return "USER_NOT_FOUND";
            }

            String salt = request.getSalt();

            String hashedDbPassword = SHAUtil.getSHA(dbPassword + salt);

            if (request.getPassword().equals(hashedDbPassword)) {

                String token = jwtUtil.generateToken(request.getUsername());
                return token;

            } else {
                return "INVALID_PASSWORD";
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}