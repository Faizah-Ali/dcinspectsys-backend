package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationResponse;

import com.dhc.inspection_system.dto.PaginatedResponse;

public interface ApplicationDAO {

    ApplicationDetailsResponse getApplicationDetails(int diaryNo, int diaryYr);

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