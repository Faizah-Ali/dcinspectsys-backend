package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.AssignApplicationRequest;
import com.dhc.inspection_system.dto.PaginatedResponse;
import com.dhc.inspection_system.dto.SendForApprovalRequest;

import java.util.List;

public interface ApplicationDAO {

    ApplicationDetailsResponse getApplicationDetails(int diaryNo, int diaryYr);

    PaginatedResponse<ApplicationResponse> getApplications(
            String owner,
            String assigned,
            String applappby,
            List<String> statuses,
            String search,
            String caseStatus,
            String applicationStatus,
            boolean unassignedOnly,
            int page,
            int size
    );

    int assignApplication(AssignApplicationRequest request);

    int approveApplication(int diaryNo, int diaryYr, String remarks);

    int rejectApplication(int diaryNo, int diaryYr, String remarks);

    int sendForApproval(SendForApprovalRequest request);
}
