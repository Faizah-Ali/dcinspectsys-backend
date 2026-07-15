package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.auth.JwtResponse;
import com.dhc.inspection_system.auth.JwtUtil;
import com.dhc.inspection_system.dao.LoginDAO;
import com.dhc.inspection_system.dto.LoginRequest;
import com.dhc.inspection_system.dto.LoginUserDTO;
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
    public JwtResponse login(LoginRequest request) {

        try {

            LoginUserDTO user = loginDAO.getPasswordByUsername(request.getUsername());

            if (user == null) {
                return new JwtResponse(null, null, null, "USER_NOT_FOUND");
            }

            String salt = request.getSalt();

            String hashedDbPassword = SHAUtil.getSHA(user.getPassword() + salt);

            if (request.getPassword().equals(hashedDbPassword)) {

                String token = jwtUtil.generateToken(request.getUsername());
                return new JwtResponse(
                        token,
                        request.getUsername(),
                        user.getRole(),
                        "Login Successful"
                );

            } else {
                return new JwtResponse(null, null, null, "INVALID_PASSWORD");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
