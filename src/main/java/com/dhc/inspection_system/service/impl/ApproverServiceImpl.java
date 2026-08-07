package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.auth.JwtUtil;
import com.dhc.inspection_system.dao.ApproverDao;
import com.dhc.inspection_system.dao.LoginDAO;
import com.dhc.inspection_system.dto.ApproverResponse;
import com.dhc.inspection_system.dto.LoginUserDTO;
import com.dhc.inspection_system.service.ApproverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApproverServiceImpl implements ApproverService {

    @Autowired
    private ApproverDao approverDao;

    @Autowired
    private LoginDAO loginDAO;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public List<ApproverResponse> getApproversList() {
        return approverDao.getApproversList();
    }

    @Override
    public List<ApproverResponse> getInspectionApprovers(String authorization) {
        String username = extractUsernameFromAuthorization(authorization);
        if (username == null || username.isBlank()) {
            return List.of();
        }

        LoginUserDTO loggedInUser = loginDAO.getUserByUsername(username);
        String branchId = loggedInUser != null ? loggedInUser.getGroup() : null;

        return approverDao.getInspectionApprovers(branchId, username);
    }

    private String extractUsernameFromAuthorization(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim()
                : authorization.trim();

        if (token.isBlank() || !jwtUtil.validateToken(token)) {
            return null;
        }

        return jwtUtil.extractUsername(token);
    }
}
