package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationOwnershipInfo;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.AssignApplicationRequest;
import com.dhc.inspection_system.dto.CourtFeeQueryResult;
import com.dhc.inspection_system.dto.ForwardApplicationRequest;
import com.dhc.inspection_system.dto.PaginatedResponse;
import com.dhc.inspection_system.dto.SendForApprovalRequest;

import java.sql.Timestamp;
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
            int size,
            ApplicationOrderMode orderMode
    );

    int assignApplication(AssignApplicationRequest request);

    int approveApplication(int diaryNo, int diaryYr, String remarks);

    int rejectApplication(int diaryNo, int diaryYr, String remarks);

    int rejectApplicationByOfficer(int diaryNo, int diaryYr, String remarks);

    int sendForApproval(
            SendForApprovalRequest request,
            String officerUsername,
            String officerFullName
    );

    int forwardApplication(ForwardApplicationRequest request);

    int completeApplication(int diaryNo, int diaryYr, String remarks);

    /**
     * Legacy EditDao.update_court_fee — updates amount / locked flag / ecourt message only.
     */
    boolean updateCourtFee(int diaryNo, int diaryYr, CourtFeeQueryResult result);

    Timestamp getCycleCutoff(int diaryNo, int diaryYr);

    boolean hasDataShareReceiverDetails(int diaryNo, int diaryYr, Timestamp cycleCutoff);

    boolean hasOnlineInspectionMessage(int diaryNo, int diaryYr, Timestamp cycleCutoff);

    ApplicationOwnershipInfo getStatusAndApplappby(int diaryNo, int diaryYr);
}
