package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dto.AssignApplicationRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDAOImplTest {

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
        assertTrue(sql.contains("reject_complete_date = CURRENT_TIMESTAMP"));
        assertTrue(sql.indexOf("reject_complete_date") < sql.indexOf("WHERE diary_no"));
        assertTrue(argsCaptor.getValue().length == 5);
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
}
