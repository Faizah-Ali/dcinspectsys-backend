package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.auth.JwtUtil;
import com.dhc.inspection_system.dao.ApplicationDAO;
import com.dhc.inspection_system.dao.LoginDAO;
import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.CompleteApplicationRequest;
import com.dhc.inspection_system.dto.LoginUserDTO;
import com.dhc.inspection_system.service.CourtFeeService;
import com.dhc.inspection_system.service.EmailQueueService;
import com.dhc.inspection_system.service.InspectionAuditService;
import com.dhc.inspection_system.service.SmsQueueService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    private static final int DIARY_NO = 148426;
    private static final int DIARY_YR = 2026;
    private static final String OFFICER = "63035467";

    @Mock
    private ApplicationDAO applicationDAO;

    @Mock
    private LoginDAO loginDAO;

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

        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.extractUsername("token")).thenReturn(OFFICER);
        when(loginDAO.getUserByUsername(OFFICER)).thenReturn(user);
        when(applicationDAO.getApplicationDetails(DIARY_NO, DIARY_YR))
                .thenReturn(application);
        when(applicationDAO.getCycleCutoff(DIARY_NO, DIARY_YR))
                .thenReturn(cycleCutoff);
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
    }
}
