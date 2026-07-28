package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.auth.JwtUtil;
import com.dhc.inspection_system.dao.ApplicationDAO;
import com.dhc.inspection_system.dao.LoginDAO;
import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationOwnershipInfo;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.ApproveRejectRequest;
import com.dhc.inspection_system.dto.AssignApplicationRequest;
import com.dhc.inspection_system.dto.CompleteApplicationRequest;
import com.dhc.inspection_system.dto.ForwardApplicationRequest;
import com.dhc.inspection_system.dto.LoginUserDTO;
import com.dhc.inspection_system.dto.PaginatedResponse;
import com.dhc.inspection_system.dto.SendForApprovalRequest;
import com.dhc.inspection_system.service.ApplicationService;
import com.dhc.inspection_system.service.InspectionAuditService;
import com.dhc.inspection_system.utils.InspectionAuditLogHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    // TEMP debug logger for approve audit investigation.
    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private LoginDAO loginDAO;

    @Autowired
    private InspectionAuditService inspectionAuditService;

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

        // Role-based inbox filters apply only when no explicit applicationStatus
        // is requested (Processed/Rejected pages pass applicationStatus=Y/C).
        boolean inboxRequest =
                applicationStatus == null || applicationStatus.isBlank();

        // Admin Inbox (INSPECTIONADMIN): force legacy filters from DB role/group.
        if ("INSPECTIONADMIN".equals(loggedInRole) && inboxRequest) {
            owner = loggedInGroup;
            statuses = List.of("N");
            unassignedOnly = true;
        }

        // Pending Applications (ONLINEINSPECTION): assigned to logged-in user.
        if ("ONLINEINSPECTION".equals(loggedInRole) && inboxRequest) {
            owner = null;
            assigned = loggedInUsername;
            statuses = List.of("N", "P", "T", "K");
            unassignedOnly = false;
        }

        // Approver Inbox (INSPECTIONAPPROVER): applications submitted for approval by user.
        if ("INSPECTIONAPPROVER".equals(loggedInRole) && inboxRequest) {
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
    public int assignApplication(String authorization, AssignApplicationRequest request) {
        String loggedInUsername = extractUsernameFromAuthorization(authorization);
        LoginUserDTO loggedInUser = null;

        if (loggedInUsername != null && !loggedInUsername.isBlank()) {
            loggedInUser = loginDAO.getUserByUsername(loggedInUsername);
        }

        String loggedInRole = loggedInUser != null ? loggedInUser.getRole() : null;

        if (!"INSPECTIONADMIN".equals(loggedInRole)) {
            throw new AccessDeniedException(
                    "Only Inspection Admin can assign applications."
            );
        }

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
    @Transactional(rollbackFor = Exception.class)
    public int approveApplication(String authorization, ApproveRejectRequest request) {
        log.info("[APPROVE-AUDIT] approveApplication entered: diaryNo={}, diaryYr={}",
                request.getDiaryNo(), request.getDiaryYr());

        if (request.getDiaryNo() == null) {
            throw new IllegalArgumentException("diaryNo must not be null");
        }

        if (request.getDiaryYr() == null) {
            throw new IllegalArgumentException("diaryYr must not be null");
        }

        String loggedInUsername = extractUsernameFromAuthorization(authorization);
        log.info("[APPROVE-AUDIT] JWT username extracted: {}", loggedInUsername);
        LoginUserDTO loggedInUser = null;

        if (loggedInUsername != null && !loggedInUsername.isBlank()) {
            loggedInUser = loginDAO.getUserByUsername(loggedInUsername);
        }

        String loggedInRole = loggedInUser != null ? loggedInUser.getRole() : null;

        if (!"INSPECTIONAPPROVER".equals(loggedInRole)) {
            throw new AccessDeniedException(
                    "Only Inspection Approver can approve applications."
            );
        }

        validateApproverOwnership(
                request.getDiaryNo(),
                request.getDiaryYr(),
                loggedInUsername
        );

        ApplicationDetailsResponse applicationDetails =
                applicationDAO.getApplicationDetails(
                        request.getDiaryNo(),
                        request.getDiaryYr()
                );

        try {
            int updatedRows = applicationDAO.approveApplication(
                    request.getDiaryNo(),
                    request.getDiaryYr(),
                    request.getRemarks()
            );
            log.info("[APPROVE-AUDIT] updatedRows={}", updatedRows);

            if (updatedRows > 0) {
                log.info("[APPROVE-AUDIT] entering if(updatedRows > 0)");
                String approverName = applicationDetails != null
                        ? applicationDetails.getApplappbyname()
                        : "";
                String description = InspectionAuditLogHelper.buildApproveDescription(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        approverName,
                        loggedInUsername,
                        request.getRemarks()
                );
                log.info("[APPROVE-AUDIT] description generated: {}", description);

                log.info("[APPROVE-AUDIT] before calling inspectionAuditService.saveInspectionAuditLog");
                int logRows = inspectionAuditService.saveInspectionAuditLog(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        description,
                        loggedInUsername
                );
                log.info("[APPROVE-AUDIT] returned logRows={}", logRows);

                if (logRows <= 0) {
                    throw new RuntimeException("Failed to insert efiling_log");
                }
            }

            log.info("[APPROVE-AUDIT] method exit: returning updatedRows={}", updatedRows);
            return updatedRows;
        } catch (Exception e) {
            log.info("[APPROVE-AUDIT] exception in approveApplication: {}", e.toString());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectApplication(String authorization, ApproveRejectRequest request) {
        if (request.getDiaryNo() == null) {
            throw new IllegalArgumentException("diaryNo must not be null");
        }

        if (request.getDiaryYr() == null) {
            throw new IllegalArgumentException("diaryYr must not be null");
        }

        try {
            String loggedInUsername = extractUsernameFromAuthorization(authorization);
            if (loggedInUsername == null || loggedInUsername.isBlank()) {
                throw new IllegalArgumentException("Authorization is required");
            }

            LoginUserDTO loggedInUser = loginDAO.getUserByUsername(loggedInUsername);
            String loggedInRole = loggedInUser != null ? loggedInUser.getRole() : null;

            if (!"ONLINEINSPECTION".equals(loggedInRole)
                    && !"INSPECTIONAPPROVER".equals(loggedInRole)) {
                throw new AccessDeniedException(
                        "Only Inspection Officer or Inspection Approver can reject applications."
                );
            }

            if ("INSPECTIONAPPROVER".equals(loggedInRole)) {
                validateApproverOwnership(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        loggedInUsername
                );
            }

            ApplicationDetailsResponse applicationDetails =
                    applicationDAO.getApplicationDetails(
                            request.getDiaryNo(),
                            request.getDiaryYr()
                    );

            int updatedRows;
            if ("ONLINEINSPECTION".equals(loggedInRole)) {
                String currentStatus = applicationDetails != null
                        ? applicationDetails.getStatus()
                        : null;
                if (!"P".equals(currentStatus) && !"K".equals(currentStatus)) {
                    throw new AccessDeniedException(
                            "Application cannot be rejected in its current status."
                    );
                }

                String assigned = applicationDetails != null
                        ? applicationDetails.getAssigned()
                        : null;
                if (assigned == null || !assigned.equals(loggedInUsername)) {
                    throw new AccessDeniedException(
                            "You are not authorized to process this application."
                    );
                }

                updatedRows = applicationDAO.rejectApplicationByOfficer(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        request.getRemarks()
                );
            } else {
                updatedRows = applicationDAO.rejectApplication(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        request.getRemarks()
                );
            }

            if (updatedRows > 0) {
                String rejectorName;
                if ("ONLINEINSPECTION".equals(loggedInRole)) {
                    rejectorName = applicationDetails != null
                            ? applicationDetails.getAssignedname()
                            : "";
                } else {
                    rejectorName = applicationDetails != null
                            ? applicationDetails.getApplappbyname()
                            : "";
                }
                String description = InspectionAuditLogHelper.buildRejectDescription(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        rejectorName,
                        loggedInUsername,
                        request.getRemarks()
                );

                int logRows = inspectionAuditService.saveInspectionAuditLog(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        description,
                        loggedInUsername
                );

                if (logRows <= 0) {
                    throw new RuntimeException("Failed to insert efiling_log");
                }
            }

            return updatedRows;
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void validateApproverOwnership(int diaryNo, int diaryYr, String loggedInUsername) {
        ApplicationOwnershipInfo ownershipInfo =
                applicationDAO.getStatusAndApplappby(diaryNo, diaryYr);

        if (ownershipInfo == null) {
            throw new IllegalArgumentException("Application not found.");
        }

        if (!"T".equals(ownershipInfo.getStatus())) {
            throw new AccessDeniedException(
                    "Application is not pending approval."
            );
        }

        String applappby = ownershipInfo.getApplappby();
        if (applappby == null
                || loggedInUsername == null
                || !applappby.equals(loggedInUsername)) {
            throw new AccessDeniedException(
                    "You are not authorized to process this application."
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sendForApproval(String authorization, SendForApprovalRequest request) {
        if (request.getDiaryNo() == null) {
            throw new IllegalArgumentException("diaryNo must not be null");
        }

        if (request.getDiaryYr() == null) {
            throw new IllegalArgumentException("diaryYr must not be null");
        }

        if (request.getApproverId() == null || request.getApproverId().isBlank()) {
            throw new IllegalArgumentException("approverId must not be null or blank");
        }

        if (request.getApproverName() == null || request.getApproverName().isBlank()) {
            throw new IllegalArgumentException("approverName must not be null or blank");
        }

        String actor = extractUsernameFromAuthorization(authorization);
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("Authorization is required");
        }

        LoginUserDTO loggedInUser = loginDAO.getUserByUsername(actor);
        String loggedInRole = loggedInUser != null ? loggedInUser.getRole() : null;

        if (!"ONLINEINSPECTION".equals(loggedInRole)) {
            throw new AccessDeniedException(
                    "Only Inspection Officer can send applications for approval."
            );
        }

        ApplicationDetailsResponse applicationDetails =
                applicationDAO.getApplicationDetails(
                        request.getDiaryNo(),
                        request.getDiaryYr()
                );

        String currentStatus = applicationDetails != null
                ? applicationDetails.getStatus()
                : null;
        if (!"N".equals(currentStatus)) {
            throw new AccessDeniedException(
                    "Application cannot be sent for approval in its current status."
            );
        }

        String assigned = applicationDetails != null
                ? applicationDetails.getAssigned()
                : null;
        if (assigned == null || !assigned.equals(actor)) {
            throw new AccessDeniedException(
                    "You are not authorized to process this application."
            );
        }

        try {
            int updatedRows = applicationDAO.sendForApproval(request);

            if (updatedRows > 0) {
                String description = InspectionAuditLogHelper.buildForwardDescription(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        request.getApproverName(),
                        request.getApproverId(),
                        request.getRemarks()
                );

                int logRows = inspectionAuditService.saveInspectionAuditLog(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        description,
                        actor
                );

                if (logRows <= 0) {
                    throw new RuntimeException("Failed to insert efiling_log");
                }
            }

            return updatedRows;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int forwardApplication(String authorization, ForwardApplicationRequest request) {
        if (request.getDiaryNo() == null) {
            throw new IllegalArgumentException("diaryNo must not be null");
        }

        if (request.getDiaryYr() == null) {
            throw new IllegalArgumentException("diaryYr must not be null");
        }

        if (request.getApproverId() == null || request.getApproverId().isBlank()) {
            throw new IllegalArgumentException("approverId must not be null or blank");
        }

        if (request.getApproverName() == null || request.getApproverName().isBlank()) {
            throw new IllegalArgumentException("approverName must not be null or blank");
        }

        String actor = extractUsernameFromAuthorization(authorization);
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("Authorization is required");
        }

        LoginUserDTO loggedInUser = loginDAO.getUserByUsername(actor);
        String loggedInRole = loggedInUser != null ? loggedInUser.getRole() : null;

        if (!"INSPECTIONAPPROVER".equals(loggedInRole)) {
            throw new AccessDeniedException(
                    "Only Inspection Approver can forward applications."
            );
        }

        validateApproverOwnership(
                request.getDiaryNo(),
                request.getDiaryYr(),
                actor
        );

        try {
            int updatedRows = applicationDAO.forwardApplication(request);

            if (updatedRows > 0) {
                String description = InspectionAuditLogHelper.buildApproverForwardDescription(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        request.getApproverName(),
                        request.getApproverId(),
                        request.getRemarks()
                );

                int logRows = inspectionAuditService.saveInspectionAuditLog(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        description,
                        actor
                );

                if (logRows <= 0) {
                    throw new RuntimeException("Failed to insert efiling_log");
                }
            }

            return updatedRows;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completeApplication(String authorization, CompleteApplicationRequest request) {
        if (request.getDiaryNo() == null || request.getDiaryYr() == null) {
            throw new IllegalArgumentException("Diary number and year are required.");
        }

        String loggedInUsername = extractUsernameFromAuthorization(authorization);
        if (loggedInUsername == null || loggedInUsername.isBlank()) {
            throw new IllegalArgumentException("Authorization is required");
        }

        LoginUserDTO loggedInUser = loginDAO.getUserByUsername(loggedInUsername);
        String loggedInRole = loggedInUser != null ? loggedInUser.getRole() : null;

        if (!"ONLINEINSPECTION".equals(loggedInRole)) {
            throw new AccessDeniedException(
                    "Only Inspection Officer can complete applications."
            );
        }

        ApplicationDetailsResponse applicationDetails =
                applicationDAO.getApplicationDetails(
                        request.getDiaryNo(),
                        request.getDiaryYr()
                );

        String currentStatus = applicationDetails != null
                ? applicationDetails.getStatus()
                : null;
        if (!"P".equals(currentStatus)) {
            throw new AccessDeniedException(
                    "Application cannot be completed in its current status."
            );
        }

        String assigned = applicationDetails != null
                ? applicationDetails.getAssigned()
                : null;
        if (assigned == null || !assigned.equals(loggedInUsername)) {
            throw new AccessDeniedException(
                    "You are not authorized to process this application."
            );
        }

        try {
            if (!applicationDAO.hasDataShareReceiverDetails(
                    request.getDiaryNo(),
                    request.getDiaryYr()
            )) {
                throw new IllegalArgumentException(
                        "Please upload the file for e-Inspection. Approval of inspection failed."
                );
            }

            int updatedRows = applicationDAO.completeApplication(
                    request.getDiaryNo(),
                    request.getDiaryYr(),
                    request.getRemarks()
            );

            if (updatedRows > 0) {
                ApplicationDetailsResponse completedDetails =
                        applicationDAO.getApplicationDetails(
                                request.getDiaryNo(),
                                request.getDiaryYr()
                        );

                String fullName;
                if ("ONLINEINSPECTION".equals(loggedInRole)) {
                    fullName = completedDetails != null
                            ? completedDetails.getAssignedname()
                            : "";
                } else {
                    fullName = completedDetails != null
                            ? completedDetails.getApplappbyname()
                            : "";
                }
                if (fullName == null) {
                    fullName = "";
                }

                String description = InspectionAuditLogHelper.buildCompleteDescription(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        loggedInUsername,
                        fullName
                );

                int logRows = inspectionAuditService.saveInspectionAuditLog(
                        request.getDiaryNo(),
                        request.getDiaryYr(),
                        description,
                        loggedInUsername
                );

                if (logRows <= 0) {
                    throw new RuntimeException("Failed to insert efiling_log");
                }
            }

            return updatedRows;
        } catch (IllegalArgumentException e) {
            throw e;
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
