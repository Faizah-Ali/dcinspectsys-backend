package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.ApplicationOrderMode;
import com.dhc.inspection_system.dto.AssignApplicationRequest;
import com.dhc.inspection_system.dto.SendForApprovalRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDAOImplTest {

    private static final String PRESERVE_BLANK_REMARKS =
            "COALESCE(NULLIF(TRIM(?), ''), remarks)";

    private static final int DIARY_NO = 148426;
    private static final int DIARY_YR = 2026;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ApplicationDAOImpl applicationDAO;

    @Test
    void assignApplicationStartsNewCycleWithoutChangingExistingParameters() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        AssignApplicationRequest request = new AssignApplicationRequest();
        request.setDiaryNo(148426);
        request.setDiaryYr(2026);
        request.setAssigned("officer");
        request.setAssignedname("Officer Name");
        request.setRemarks("Reassigned");

        applicationDAO.assignApplication(request);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), argsCaptor.capture());

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("status = 'N'"));
        assertTrue(sql.contains(PRESERVE_BLANK_REMARKS));
        assertTrue(sql.contains("reject_complete_date = CURRENT_TIMESTAMP"));
        assertTrue(sql.indexOf("reject_complete_date") < sql.indexOf("WHERE diary_no"));
        assertTrue(argsCaptor.getValue().length == 5);
        assertEquals("Reassigned", argsCaptor.getValue()[2]);
    }

    @Test
    void nonEmptyRemarksStillOverwriteOnAllWorkflowUpdates() {
        assertRemarksBindAndSql("New remark");
    }

    @Test
    void emptyRemarksPreserveExistingValueOnAllWorkflowUpdates() {
        assertRemarksBindAndSql("");
    }

    @Test
    void whitespaceOnlyRemarksPreserveExistingValueOnAllWorkflowUpdates() {
        assertRemarksBindAndSql("   ");
    }

    @Test
    void nullRemarksPreserveExistingValueOnAllWorkflowUpdates() {
        assertRemarksBindAndSql(null);
    }

    @SuppressWarnings("unchecked")
    @Test
    void latestActionUsesEfilingLogMaxEntryDate() {
        stubListQuery();

        applicationDAO.getApplications(
                null, null, null, null, null, null, null, false, 1, 10,
                ApplicationOrderMode.LATEST_ACTION
        );

        assertLatestActionOrderSql(captureListSql());
    }

    @SuppressWarnings("unchecked")
    @Test
    void nullOrderModeUsesLatestActionOrdering() {
        stubListQuery();

        applicationDAO.getApplications(
                null, null, null, null, null, null, null, false, 1, 10,
                null
        );

        assertLatestActionOrderSql(captureListSql());
    }

    private void assertLatestActionOrderSql(String sql) {
        assertTrue(sql.contains("efiling_log"),
                "LATEST_ACTION must query efiling_log");
        assertTrue(sql.contains("MAX(e.entry_date)"),
                "LATEST_ACTION must use MAX(e.entry_date)");
        assertTrue(sql.contains("e.source = 'e-Inspection'"),
                "LATEST_ACTION must restrict source to e-Inspection");
        assertTrue(sql.contains("DESC NULLS LAST"),
                "LATEST_ACTION must be DESC NULLS LAST");
        assertTrue(sql.contains("diary_yr DESC"),
                "LATEST_ACTION must keep diary_yr as a tie-breaker");
        assertTrue(sql.contains("diary_no DESC"),
                "LATEST_ACTION must keep diary_no as a tie-breaker");
        assertFalse(sql.contains("for approval"),
                "LATEST_ACTION must not filter by description");
        assertFalse(sql.contains("for further directions"),
                "LATEST_ACTION must not filter by description");
        assertFalse(sql.contains("applied_date DESC"),
                "LATEST_ACTION must not use applied_date");
        assertFalse(sql.contains("reject_complete_date DESC"),
                "LATEST_ACTION must not use reject_complete_date");
    }

    @SuppressWarnings("unchecked")
    private void stubListQuery() {
        reset(jdbcTemplate);
        when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenReturn(0L);
    }

    @SuppressWarnings("unchecked")
    private String captureListSql() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(
                sqlCaptor.capture(), any(Object[].class), any(RowMapper.class)
        );
        return sqlCaptor.getValue();
    }

    @Test
    void oldUploadDoesNotSatisfyCurrentCycleCheck() {
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(Object[].class)
        )).thenReturn(0);

        Timestamp cutoff = Timestamp.valueOf("2026-08-17 12:00:00");

        assertFalse(applicationDAO.hasDataShareReceiverDetails(148426, 2026, cutoff));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).queryForObject(
                sqlCaptor.capture(),
                eq(Integer.class),
                argsCaptor.capture()
        );

        assertTrue(sqlCaptor.getValue().contains("d.file_upload_flag = 'A'"));
        assertTrue(sqlCaptor.getValue().contains(
                "d.entry_date > COALESCE(?, '-infinity'::timestamp)"
        ));
        assertTrue(argsCaptor.getValue()[2].equals(cutoff));
    }

    @Test
    void currentUploadWithMatchingMessageSatisfiesMessageCheck() {
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(Object[].class)
        )).thenReturn(1);

        Timestamp cutoff = Timestamp.valueOf("2026-08-17 12:00:00");

        assertTrue(applicationDAO.hasOnlineInspectionMessage(148426, 2026, cutoff));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(
                sqlCaptor.capture(),
                eq(Integer.class),
                any(Object[].class)
        );

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("d.file_upload_flag = 'A'"));
        assertTrue(sql.contains("d.entry_date > COALESCE(?, '-infinity'::timestamp)"));
        assertTrue(sql.contains("m.message LIKE '%a=' || d.uniqueid || '%'"));
    }

    private void assertRemarksBindAndSql(String remarks) {
        stubUpdate();
        CapturedUpdate assign = captureAssign(remarks);
        assertRemarksPreserveSql(assign.sql);
        assertEquals(remarks, assign.args[2]);

        stubUpdate();
        CapturedUpdate send = captureSendForApproval(remarks);
        assertRemarksPreserveSql(send.sql);
        assertEquals(remarks, send.args[4]);

        stubUpdate();
        CapturedUpdate approve = captureSimpleRemarksUpdate(
                () -> applicationDAO.approveApplication(DIARY_NO, DIARY_YR, remarks)
        );
        assertRemarksPreserveSql(approve.sql);
        assertEquals(remarks, approve.args[0]);

        stubUpdate();
        CapturedUpdate complete = captureSimpleRemarksUpdate(
                () -> applicationDAO.completeApplication(DIARY_NO, DIARY_YR, remarks)
        );
        assertRemarksPreserveSql(complete.sql);
        assertEquals(remarks, complete.args[0]);

        stubUpdate();
        CapturedUpdate officerReject = captureSimpleRemarksUpdate(
                () -> applicationDAO.rejectApplicationByOfficer(DIARY_NO, DIARY_YR, remarks)
        );
        assertRemarksPreserveSql(officerReject.sql);
        assertEquals(remarks, officerReject.args[0]);

        stubUpdate();
        CapturedUpdate approverReject = captureSimpleRemarksUpdate(
                () -> applicationDAO.rejectApplication(DIARY_NO, DIARY_YR, remarks)
        );
        assertRemarksPreserveSql(approverReject.sql);
        assertEquals(remarks, approverReject.args[0]);
    }

    private void stubUpdate() {
        reset(jdbcTemplate);
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
    }

    private void assertRemarksPreserveSql(String sql) {
        assertTrue(sql.contains(PRESERVE_BLANK_REMARKS));
        assertFalse(sql.contains("remarks=?"));
        assertFalse(sql.contains("remarks = ?,"));
    }

    private CapturedUpdate captureAssign(String remarks) {
        AssignApplicationRequest request = new AssignApplicationRequest();
        request.setDiaryNo(DIARY_NO);
        request.setDiaryYr(DIARY_YR);
        request.setAssigned("officer");
        request.setAssignedname("Officer Name");
        request.setRemarks(remarks);

        applicationDAO.assignApplication(request);
        return captureUpdate();
    }

    private CapturedUpdate captureSendForApproval(String remarks) {
        SendForApprovalRequest request = new SendForApprovalRequest();
        request.setDiaryNo(DIARY_NO);
        request.setDiaryYr(DIARY_YR);
        request.setApproverId("approver");
        request.setApproverName("Approver Name");
        request.setRemarks(remarks);

        applicationDAO.sendForApproval(request, "officer", "Officer Name");
        return captureUpdate();
    }

    private CapturedUpdate captureSimpleRemarksUpdate(Runnable action) {
        action.run();
        return captureUpdate();
    }

    private CapturedUpdate captureUpdate() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), argsCaptor.capture());
        return new CapturedUpdate(sqlCaptor.getValue(), argsCaptor.getValue());
    }

    private record CapturedUpdate(String sql, Object[] args) {
    }
}
