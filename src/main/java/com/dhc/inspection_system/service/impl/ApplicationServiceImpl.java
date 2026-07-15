package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.auth.JwtUtil;
import com.dhc.inspection_system.dao.ApplicationDAO;
import com.dhc.inspection_system.dao.LoginDAO;
import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.ApproveRejectRequest;
import com.dhc.inspection_system.dto.AssignApplicationRequest;
import com.dhc.inspection_system.dto.LoginUserDTO;
import com.dhc.inspection_system.dto.PaginatedResponse;
import com.dhc.inspection_system.service.ApplicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private LoginDAO loginDAO;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public ApplicationDetailsResponse getApplicationDetails(int diaryNo, int diaryYr) {
        return applicationDAO.getApplicationDetails(diaryNo, diaryYr);
    }

    @Override
    public PaginatedResponse<ApplicationResponse> getApplications(
            String authorization,
            String owner,
            String status,
            String search,
            String caseStatus,
            String applicationStatus,
            int page,
            int size
    ) {

        // Identify logged-in user from JWT only (not from request params).
        String loggedInUsername = extractUsernameFromAuthorization(authorization);
        LoginUserDTO loggedInUser = null;

        if (loggedInUsername != null && !loggedInUsername.isBlank()) {
            loggedInUser = loginDAO.getUserByUsername(loggedInUsername);
        }

        String loggedInRole = loggedInUser != null ? loggedInUser.getRole() : null;
        String loggedInGroup = loggedInUser != null ? loggedInUser.getGroup() : null;

        boolean unassignedOnly = false;
        String assigned = null;
        String applappby = null;
        List<String> statuses = (status != null && !status.isBlank())
                ? List.of(status)
                : null;

        // Admin Inbox (INSPECTIONADMIN): force legacy filters from DB role/group.
        if ("INSPECTIONADMIN".equals(loggedInRole)) {
            owner = loggedInGroup;
            statuses = List.of("N");
            unassignedOnly = true;
        }

        // Pending Applications (ONLINEINSPECTION): assigned to logged-in user.
        if ("ONLINEINSPECTION".equals(loggedInRole)) {
            owner = null;
            assigned = loggedInUsername;
            statuses = List.of("N", "P", "T", "K");
            unassignedOnly = false;
        }

        // Approver Inbox (INSPECTIONAPPROVER): applications submitted for approval by user.
        if ("INSPECTIONAPPROVER".equals(loggedInRole)) {
            owner = null;
            assigned = null;
            applappby = loggedInUsername;
            statuses = List.of("T");
            unassignedOnly = false;
        }

        return applicationDAO.getApplications(
                owner,
                assigned,
                applappby,
                statuses,
                search,
                caseStatus,
                applicationStatus,
                unassignedOnly,
                page,
                size
        );
    }

    @Override
    public int assignApplication(AssignApplicationRequest request) {
        if (request.getDiaryNo() == null) {
            throw new IllegalArgumentException("diaryNo must not be null");
        }

        if (request.getDiaryYr() == null) {
            throw new IllegalArgumentException("diaryYr must not be null");
        }

        if (request.getAssigned() == null || request.getAssigned().isBlank()) {
            throw new IllegalArgumentException("assigned must not be null or blank");
        }

        if (request.getAssignedname() == null || request.getAssignedname().isBlank()) {
            throw new IllegalArgumentException("assignedname must not be null or blank");
        }

        try {
            return applicationDAO.assignApplication(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public int approveApplication(ApproveRejectRequest request) {
        if (request.getDiaryNo() == null) {
            throw new IllegalArgumentException("diaryNo must not be null");
        }

        if (request.getDiaryYr() == null) {
            throw new IllegalArgumentException("diaryYr must not be null");
        }

        try {
            return applicationDAO.approveApplication(
                    request.getDiaryNo(),
                    request.getDiaryYr()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public int rejectApplication(ApproveRejectRequest request) {
        if (request.getDiaryNo() == null) {
            throw new IllegalArgumentException("diaryNo must not be null");
        }

        if (request.getDiaryYr() == null) {
            throw new IllegalArgumentException("diaryYr must not be null");
        }

        try {
            return applicationDAO.rejectApplication(
                    request.getDiaryNo(),
                    request.getDiaryYr()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String extractUsernameFromAuthorization(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        // Service removes "Bearer " prefix.
        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim()
                : authorization.trim();

        if (token.isBlank() || !jwtUtil.validateToken(token)) {
            return null;
        }

        return jwtUtil.extractUsername(token);
    }
}
