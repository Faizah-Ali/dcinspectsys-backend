package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.ApplicationResponse;

import com.dhc.inspection_system.dto.PaginatedResponse;

public interface ApplicationDAO {

    PaginatedResponse<ApplicationResponse> getApplications(
            String owner,
            String status,
            String search,
            String caseStatus,
            String applicationStatus,
            int page,
            int size
    );
}