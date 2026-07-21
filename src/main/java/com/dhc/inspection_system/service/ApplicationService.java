package com.dhc.inspection_system.service;

import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.ApproveRejectRequest;
import com.dhc.inspection_system.dto.AssignApplicationRequest;
import com.dhc.inspection_system.dto.CompleteApplicationRequest;
import com.dhc.inspection_system.dto.ForwardApplicationRequest;
import com.dhc.inspection_system.dto.PaginatedResponse;
import com.dhc.inspection_system.dto.SendForApprovalRequest;

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

    int assignApplication(String authorization, AssignApplicationRequest request);

    int approveApplication(ApproveRejectRequest request);

    int rejectApplication(String authorization, ApproveRejectRequest request);

    int sendForApproval(SendForApprovalRequest request);

    int forwardApplication(ForwardApplicationRequest request);

    int completeApplication(CompleteApplicationRequest request);
}
