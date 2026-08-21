package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.auth.JwtUtil;
import com.dhc.inspection_system.dao.ApplicationDAO;
import com.dhc.inspection_system.dao.ApplicationOrderMode;
import com.dhc.inspection_system.dao.LoginDAO;
import com.dhc.inspection_system.dao.UploadHistoryDAO;
import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationOwnershipInfo;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.ApproveRejectRequest;
import com.dhc.inspection_system.dto.AssignApplicationRequest;
import com.dhc.inspection_system.dto.CompleteApplicationRequest;
import com.dhc.inspection_system.dto.CourtFeeQueryResult;
import com.dhc.inspection_system.dto.ForwardApplicationRequest;
import com.dhc.inspection_system.dto.LoginUserDTO;
import com.dhc.inspection_system.dto.PaginatedResponse;
import com.dhc.inspection_system.dto.SendForApprovalRequest;
import com.dhc.inspection_system.service.CourtFeeService;
import com.dhc.inspection_system.service.EmailQueueService;
import com.dhc.inspection_system.service.InspectionAuditService;
import com.dhc.inspection_system.service.SmsQueueService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    private static final int DIARY_NO = 148426;
    private static final int DIARY_YR = 2026;
    private static final String OFFICER = "63035467";
    private static final String APPROVER = "63039999";
    private static final String ADMIN = "admin01";

    @Mock
    private ApplicationDAO applicationDAO;

    @Mock
    private LoginDAO loginDAO;

    @Mock
    private UploadHistoryDAO uploadHistoryDAO;

    @Mock
    private InspectionAuditService inspectionAuditService;

    @Mock
    private EmailQueueService emailQueueService;

    @Mock
    private SmsQueueService smsQueueService;

    @Mock
    private CourtFeeService courtFeeService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private CompleteApplicationRequest request;
    private ApplicationDetailsResponse application;
    private Timestamp cycleCutoff;

    @BeforeEach
    void setUp() {
        request = new CompleteApplicationRequest();
        request.setDiaryNo(DIARY_NO);
        request.setDiaryYr(DIARY_YR);
        request.setRemarks("Complete");

        application = new ApplicationDetailsResponse();
        application.setStatus("P");
        application.setAssigned(OFFICER);
        application.setAssignedname("Inspection Officer");

        LoginUserDTO user = new LoginUserDTO();
        user.setRole("ONLINEINSPECTION");

        cycleCutoff = Timestamp.valueOf("2026-08-17 12:00:00");

        lenient().when(jwtUtil.validateToken("token")).thenReturn(true);
        lenient().when(jwtUtil.extractUsername("token")).thenReturn(OFFICER);
        lenient().when(loginDAO.getUserByUsername(OFFICER)).thenReturn(user);
        lenient().when(applicationDAO.getApplicationDetails(DIARY_NO, DIARY_YR))
                .thenReturn(application);
        lenient().when(applicationDAO.getCycleCutoff(DIARY_NO, DIARY_YR))
                .thenReturn(cycleCutoff);
    }

    @Test
    void adminInboxGetApplicationsDoesNotQueryCourtFee() {
        LoginUserDTO admin = new LoginUserDTO();
        admin.setRole("INSPECTIONADMIN");
        admin.setGroup("A");

        when(jwtUtil.extractUsername("token")).thenReturn("admin");
        when(loginDAO.getUserByUsername("admin")).thenReturn(admin);

        ApplicationResponse row = new ApplicationResponse();
        row.setDiaryNo(DIARY_NO);
        row.setDiaryYr(DIARY_YR);
        row.setEcourtFeeId("DLCT0212D2652S977");
        row.setCourtFeeAmount("");

        PaginatedResponse<ApplicationResponse> daoResponse = new PaginatedResponse<>();
        daoResponse.setContent(List.of(row));
        when(applicationDAO.getApplications(
                eq("A"),
                isNull(),
                isNull(),
                eq(List.of("N")),
                isNull(),
                eq(""),
                eq(""),
                eq(true),
                eq(1),
                eq(10),
                eq(ApplicationOrderMode.LATEST_ACTION)
        )).thenReturn(daoResponse);

        PaginatedResponse<ApplicationResponse> response =
                applicationService.getApplications(
                        "Bearer token",
                        "A",
                        null,
                        null,
                        "",
                        "",
                        1,
                        10
                );

        assertEquals(1, response.getContent().size());
        verify(courtFeeService, never()).queryCourtFee(anyString());
        verify(applicationDAO, never()).updateCourtFee(anyInt(), anyInt(), any());
    }

    @Test
    void getApplicationDetailsRefreshesEligibleCourtFee() {
        application.setDiaryNo(DIARY_NO);
        application.setDiaryYr(DIARY_YR);
        application.setEcourtFeeId("DLCT0212D2652S977");
        application.setCourtFeeAmount("");

        when(courtFeeService.queryCourtFee("DLCT0212D2652S977"))
                .thenReturn(CourtFeeQueryResult.success("5", false, "VALID COURT FEE"));

        ApplicationDetailsResponse details =
                applicationService.getApplicationDetails(DIARY_NO, DIARY_YR);

        assertNotNull(details);
        assertEquals("5", details.getCourtFeeAmount());
        assertEquals("VALID COURT FEE", details.getEcourtMessage());
        verify(courtFeeService).queryCourtFee("DLCT0212D2652S977");
        verify(applicationDAO).updateCourtFee(
                eq(DIARY_NO),
                eq(DIARY_YR),
                any(CourtFeeQueryResult.class)
        );
    }

    @Test
    void getApplicationDetailsReturnsWhenCourtFeeQueryErrors() {
        application.setDiaryNo(DIARY_NO);
        application.setDiaryYr(DIARY_YR);
        application.setEcourtFeeId("DLCT0212D2652S977");
        application.setCourtFeeAmount("");

        when(courtFeeService.queryCourtFee("DLCT0212D2652S977"))
                .thenReturn(CourtFeeQueryResult.error("NETWORK ISSUE"));

        ApplicationDetailsResponse details =
                applicationService.getApplicationDetails(DIARY_NO, DIARY_YR);

        assertNotNull(details);
        assertEquals("", details.getCourtFeeAmount());
        assertEquals("Error in court fee:NETWORK ISSUE", details.getEcourtMessage());
        verify(applicationDAO).updateCourtFee(
                eq(DIARY_NO),
                eq(DIARY_YR),
                any(CourtFeeQueryResult.class)
        );
    }

    @Test
    void getApplicationDetailsReturnsWhenCourtFeeQueryIsSkipped() {
        application.setDiaryNo(DIARY_NO);
        application.setDiaryYr(DIARY_YR);
        application.setEcourtFeeId("DLCT0212D2652S977");
        application.setCourtFeeAmount("");

        when(courtFeeService.queryCourtFee("DLCT0212D2652S977"))
                .thenReturn(CourtFeeQueryResult.skip());

        ApplicationDetailsResponse details =
                applicationService.getApplicationDetails(DIARY_NO, DIARY_YR);

        assertNotNull(details);
        assertEquals("", details.getCourtFeeAmount());
        verify(applicationDAO, never()).updateCourtFee(anyInt(), anyInt(), any());
    }

    @Test
    void getApplicationDetailsStillReturnsWhenSoapThrows() {
        application.setDiaryNo(DIARY_NO);
        application.setDiaryYr(DIARY_YR);
        application.setEcourtFeeId("DLCT0212D2652S977");
        application.setCourtFeeAmount("");

        when(courtFeeService.queryCourtFee("DLCT0212D2652S977"))
                .thenThrow(new RuntimeException("SHCIL down"));

        ApplicationDetailsResponse details =
                applicationService.getApplicationDetails(DIARY_NO, DIARY_YR);

        assertNotNull(details);
        assertEquals("", details.getCourtFeeAmount());
        verify(applicationDAO, never()).updateCourtFee(anyInt(), anyInt(), any());
    }

    @Test
    void completeFailsWhenOnlyHistoricalUploadExists() {
        when(applicationDAO.hasDataShareReceiverDetails(
                DIARY_NO,
                DIARY_YR,
                cycleCutoff
        )).thenReturn(false);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.completeApplication("Bearer token", request)
        );

        assertEquals(
                "Please upload the file for e-Inspection. Approval of inspection failed.",
                error.getMessage()
        );
        verify(applicationDAO, never()).completeApplication(
                DIARY_NO,
                DIARY_YR,
                request.getRemarks()
        );
        verify(emailQueueService, never()).queueEmailsForCompletedApplication(
                DIARY_NO,
                DIARY_YR,
                cycleCutoff
        );
    }

    @Test
    void completePassesCapturedCutoffToValidationAndNotificationQueues() {
        when(applicationDAO.hasDataShareReceiverDetails(
                DIARY_NO,
                DIARY_YR,
                cycleCutoff
        )).thenReturn(true);
        when(applicationDAO.hasOnlineInspectionMessage(
                DIARY_NO,
                DIARY_YR,
                cycleCutoff
        )).thenReturn(true);
        when(applicationDAO.completeApplication(DIARY_NO, DIARY_YR, "Complete"))
                .thenReturn(1);
        when(inspectionAuditService.saveInspectionAuditLog(
                eq(DIARY_NO),
                eq(DIARY_YR),
                anyString(),
                eq(OFFICER)
        )).thenReturn(1);

        assertEquals(
                1,
                applicationService.completeApplication("Bearer token", request)
        );

        verify(applicationDAO).hasDataShareReceiverDetails(
                DIARY_NO,
                DIARY_YR,
                cycleCutoff
        );
        verify(applicationDAO).hasOnlineInspectionMessage(
                DIARY_NO,
                DIARY_YR,
                cycleCutoff
        );
        verify(emailQueueService).queueEmailsForCompletedApplication(
                DIARY_NO,
                DIARY_YR,
                cycleCutoff
        );
        verify(smsQueueService).queueSmsForCompletedApplication(
                DIARY_NO,
                DIARY_YR,
                cycleCutoff
        );
        verify(uploadHistoryDAO, never()).saveOfficeComment(
                anyInt(),
                anyInt(),
                anyString(),
                anyString()
        );
    }

    @Test
    void officerRejectDoesNotWriteDropboxCommentButKeepsAuditAndEmail() {
        application.setStatus("P");
        application.setAssigned(OFFICER);
        application.setAssignedname("Inspection Officer");
        application.setEmail("applicant@example.com");

        ApproveRejectRequest rejectRequest = new ApproveRejectRequest();
        rejectRequest.setDiaryNo(DIARY_NO);
        rejectRequest.setDiaryYr(DIARY_YR);
        rejectRequest.setRemarks("Officer rejection remarks");

        when(applicationDAO.rejectApplicationByOfficer(
                DIARY_NO,
                DIARY_YR,
                "Officer rejection remarks"
        )).thenReturn(1);
        when(inspectionAuditService.saveInspectionAuditLog(
                eq(DIARY_NO),
                eq(DIARY_YR),
                anyString(),
                eq(OFFICER)
        )).thenReturn(1);

        assertEquals(
                1,
                applicationService.rejectApplication("Bearer token", rejectRequest)
        );

        verify(applicationDAO).rejectApplicationByOfficer(
                DIARY_NO,
                DIARY_YR,
                "Officer rejection remarks"
        );
        verify(inspectionAuditService).saveInspectionAuditLog(
                eq(DIARY_NO),
                eq(DIARY_YR),
                anyString(),
                eq(OFFICER)
        );
        verify(emailQueueService).queueRejectEmail(
                DIARY_NO,
                DIARY_YR,
                "applicant@example.com",
                "Officer rejection remarks"
        );
        verify(uploadHistoryDAO, never()).saveOfficeComment(
                anyInt(),
                anyInt(),
                anyString(),
                anyString()
        );
    }

    @Test
    void approverRejectWithRemarksWritesDropboxComment() {
        LoginUserDTO approver = new LoginUserDTO();
        approver.setRole("INSPECTIONAPPROVER");

        when(jwtUtil.extractUsername("token")).thenReturn(APPROVER);
        when(loginDAO.getUserByUsername(APPROVER)).thenReturn(approver);

        ApplicationOwnershipInfo ownership = new ApplicationOwnershipInfo();
        ownership.setStatus("T");
        ownership.setApplappby(APPROVER);
        when(applicationDAO.getStatusAndApplappby(DIARY_NO, DIARY_YR)).thenReturn(ownership);

        application.setApplappbyname("Approver Name");

        ApproveRejectRequest rejectRequest = new ApproveRejectRequest();
        rejectRequest.setDiaryNo(DIARY_NO);
        rejectRequest.setDiaryYr(DIARY_YR);
        rejectRequest.setRemarks("Approver rejection remarks");

        when(applicationDAO.rejectApplication(
                DIARY_NO,
                DIARY_YR,
                "Approver rejection remarks"
        )).thenReturn(1);
        when(uploadHistoryDAO.saveOfficeComment(
                DIARY_NO,
                DIARY_YR,
                "Approver rejection remarks",
                APPROVER
        )).thenReturn(1);
        when(inspectionAuditService.saveInspectionAuditLog(
                eq(DIARY_NO),
                eq(DIARY_YR),
                anyString(),
                eq(APPROVER)
        )).thenReturn(1);

        assertEquals(
                1,
                applicationService.rejectApplication("Bearer token", rejectRequest)
        );

        verify(uploadHistoryDAO).saveOfficeComment(
                DIARY_NO,
                DIARY_YR,
                "Approver rejection remarks",
                APPROVER
        );
        verify(emailQueueService, never()).queueRejectEmail(
                anyInt(),
                anyInt(),
                anyString(),
                anyString()
        );
    }

    @Test
    void assignWithRemarksWritesDropboxComment() {
        LoginUserDTO admin = new LoginUserDTO();
        admin.setRole("INSPECTIONADMIN");

        when(jwtUtil.extractUsername("token")).thenReturn(ADMIN);
        when(loginDAO.getUserByUsername(ADMIN)).thenReturn(admin);

        AssignApplicationRequest assignRequest = new AssignApplicationRequest();
        assignRequest.setDiaryNo(DIARY_NO);
        assignRequest.setDiaryYr(DIARY_YR);
        assignRequest.setAssigned(OFFICER);
        assignRequest.setAssignedname("Inspection Officer");
        assignRequest.setRemarks("Assign remarks");

        when(applicationDAO.assignApplication(assignRequest)).thenReturn(1);
        when(uploadHistoryDAO.saveOfficeComment(
                DIARY_NO,
                DIARY_YR,
                "Assign remarks",
                ADMIN
        )).thenReturn(1);
        when(inspectionAuditService.saveEfilingLog(
                eq(DIARY_NO),
                eq(DIARY_YR),
                anyString(),
                eq(ADMIN),
                anyString()
        )).thenReturn(1);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        assertEquals(
                1,
                applicationService.assignApplication("Bearer token", assignRequest)
        );

        verify(uploadHistoryDAO).saveOfficeComment(
                DIARY_NO,
                DIARY_YR,
                "Assign remarks",
                ADMIN
        );
    }

    @Test
    void sendForApprovalWithRemarksWritesDropboxComment() {
        SendForApprovalRequest sendRequest = new SendForApprovalRequest();
        sendRequest.setDiaryNo(DIARY_NO);
        sendRequest.setDiaryYr(DIARY_YR);
        sendRequest.setApproverId(APPROVER);
        sendRequest.setApproverName("Approver Name");
        sendRequest.setRemarks("Send for approval remarks");

        application.setStatus("N");
        application.setAssigned(OFFICER);
        application.setAssignedname("Inspection Officer");

        LoginUserDTO officer = new LoginUserDTO();
        officer.setRole("ONLINEINSPECTION");
        officer.setFullName("Inspection Officer");
        when(loginDAO.getUserByUsername(OFFICER)).thenReturn(officer);

        when(applicationDAO.sendForApproval(
                eq(sendRequest),
                eq(OFFICER),
                eq("Inspection Officer")
        )).thenReturn(1);
        when(uploadHistoryDAO.saveOfficeComment(
                DIARY_NO,
                DIARY_YR,
                "Send for approval remarks",
                OFFICER
        )).thenReturn(1);
        when(inspectionAuditService.saveInspectionAuditLog(
                eq(DIARY_NO),
                eq(DIARY_YR),
                anyString(),
                eq(OFFICER)
        )).thenReturn(1);

        assertEquals(
                1,
                applicationService.sendForApproval("Bearer token", sendRequest)
        );

        verify(uploadHistoryDAO).saveOfficeComment(
                DIARY_NO,
                DIARY_YR,
                "Send for approval remarks",
                OFFICER
        );
    }

    @Test
    void approveWithRemarksWritesDropboxComment() {
        LoginUserDTO approver = new LoginUserDTO();
        approver.setRole("INSPECTIONAPPROVER");

        when(jwtUtil.extractUsername("token")).thenReturn(APPROVER);
        when(loginDAO.getUserByUsername(APPROVER)).thenReturn(approver);

        ApplicationOwnershipInfo ownership = new ApplicationOwnershipInfo();
        ownership.setStatus("T");
        ownership.setApplappby(APPROVER);
        when(applicationDAO.getStatusAndApplappby(DIARY_NO, DIARY_YR)).thenReturn(ownership);

        application.setApplappbyname("Approver Name");

        ApproveRejectRequest approveRequest = new ApproveRejectRequest();
        approveRequest.setDiaryNo(DIARY_NO);
        approveRequest.setDiaryYr(DIARY_YR);
        approveRequest.setRemarks("Approve remarks");

        when(applicationDAO.approveApplication(
                DIARY_NO,
                DIARY_YR,
                "Approve remarks"
        )).thenReturn(1);
        when(uploadHistoryDAO.saveOfficeComment(
                DIARY_NO,
                DIARY_YR,
                "Approve remarks",
                APPROVER
        )).thenReturn(1);
        when(inspectionAuditService.saveInspectionAuditLog(
                eq(DIARY_NO),
                eq(DIARY_YR),
                anyString(),
                eq(APPROVER)
        )).thenReturn(1);

        assertEquals(
                1,
                applicationService.approveApplication("Bearer token", approveRequest)
        );

        verify(uploadHistoryDAO).saveOfficeComment(
                DIARY_NO,
                DIARY_YR,
                "Approve remarks",
                APPROVER
        );
    }

    @Test
    void forwardWithRemarksWritesDropboxComment() {
        LoginUserDTO approver = new LoginUserDTO();
        approver.setRole("INSPECTIONAPPROVER");

        when(jwtUtil.extractUsername("token")).thenReturn(APPROVER);
        when(loginDAO.getUserByUsername(APPROVER)).thenReturn(approver);

        ApplicationOwnershipInfo ownership = new ApplicationOwnershipInfo();
        ownership.setStatus("T");
        ownership.setApplappby(APPROVER);
        when(applicationDAO.getStatusAndApplappby(DIARY_NO, DIARY_YR)).thenReturn(ownership);

        ForwardApplicationRequest forwardRequest = new ForwardApplicationRequest();
        forwardRequest.setDiaryNo(DIARY_NO);
        forwardRequest.setDiaryYr(DIARY_YR);
        forwardRequest.setApproverId("63038888");
        forwardRequest.setApproverName("Next Approver");
        forwardRequest.setRemarks("Forward remarks");

        when(applicationDAO.forwardApplication(forwardRequest)).thenReturn(1);
        when(uploadHistoryDAO.saveOfficeComment(
                DIARY_NO,
                DIARY_YR,
                "Forward remarks",
                APPROVER
        )).thenReturn(1);
        when(inspectionAuditService.saveInspectionAuditLog(
                eq(DIARY_NO),
                eq(DIARY_YR),
                anyString(),
                eq(APPROVER)
        )).thenReturn(1);

        assertEquals(
                1,
                applicationService.forwardApplication("Bearer token", forwardRequest)
        );

        verify(uploadHistoryDAO).saveOfficeComment(
                DIARY_NO,
                DIARY_YR,
                "Forward remarks",
                APPROVER
        );
    }
}
