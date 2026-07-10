package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.ApplicationDAO;
import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.AssignApplicationRequest;
import com.dhc.inspection_system.service.ApplicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.dhc.inspection_system.dto.PaginatedResponse;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationDAO applicationDAO;

    @Override
    public ApplicationDetailsResponse getApplicationDetails(int diaryNo, int diaryYr) {
        return applicationDAO.getApplicationDetails(diaryNo, diaryYr);
    }

    @Override
    public PaginatedResponse<ApplicationResponse> getApplications(
            String owner,
            String status,
            String search,
            String caseStatus,
            String applicationStatus,
            int page,
            int size
    ) {

        return applicationDAO.getApplications(
                owner,
                status,
                search,
                caseStatus,
                applicationStatus,
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
}