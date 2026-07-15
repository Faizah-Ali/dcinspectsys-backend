package com.dhc.inspection_system.service;

import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.AssignApplicationRequest;
import com.dhc.inspection_system.dto.PaginatedResponse;

public interface ApplicationService {

    ApplicationDetailsResponse getApplicationDetails(int diaryNo, int diaryYr);

    PaginatedResponse<ApplicationResponse> getApplications(
            String authorization,
            String owner,
            String status,
            String search,
            String caseStatus,
            String applicationStatus,
            int page,
            int size
    );

    int assignApplication(AssignApplicationRequest request);
}
