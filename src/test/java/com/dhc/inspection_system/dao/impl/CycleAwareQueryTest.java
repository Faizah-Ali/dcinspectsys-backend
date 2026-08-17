package com.dhc.inspection_system.dao.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CycleAwareQueryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void historyReturnsAllRowsAndComputesCurrentCycle() {
        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                any(Object[].class)
        )).thenReturn(Collections.emptyList());

        UploadHistoryDAOImpl dao = new UploadHistoryDAOImpl();
        ReflectionTestUtils.setField(dao, "jdbcTemplate", jdbcTemplate);

        dao.getUploadHistory(148426, 2026);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                any(RowMapper.class),
                any(Object[].class)
        );

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("LEFT JOIN judl.inspection_user_online i"));
        assertTrue(sql.contains("COALESCE(d.file_upload_flag, 'A') = 'A'"));
        assertTrue(sql.contains("d.entry_date > i.reject_complete_date"));
        assertTrue(sql.contains("END AS current_cycle"));
        assertTrue(sql.contains("WHERE d.diary_no = ?"));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void emailQueueSelectsOnlyCurrentCycleMessages() {
        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                any(Object[].class)
        )).thenReturn(Collections.emptyList());

        EmailQueueDAOImpl dao = new EmailQueueDAOImpl();
        ReflectionTestUtils.setField(dao, "jdbcTemplate", jdbcTemplate);
        Timestamp cutoff = Timestamp.valueOf("2026-08-17 12:00:00");

        dao.getOnlineInspectionMessages(148426, 2026, cutoff);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                any(RowMapper.class),
                any(Object[].class)
        );

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("d.file_upload_flag = 'A'"));
        assertTrue(sql.contains("d.entry_date > COALESCE(?, '-infinity'::timestamp)"));
        assertTrue(sql.contains("m.message LIKE '%a=' || d.uniqueid || '%'"));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void smsQueueSelectsOnlyCurrentCycleMessages() {
        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                any(Object[].class)
        )).thenReturn(Collections.emptyList());

        SmsQueueDAOImpl dao = new SmsQueueDAOImpl();
        ReflectionTestUtils.setField(dao, "jdbcTemplate", jdbcTemplate);
        Timestamp cutoff = Timestamp.valueOf("2026-08-17 12:00:00");

        dao.getOnlineInspectionSmsMessages(148426, 2026, cutoff);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                any(RowMapper.class),
                any(Object[].class)
        );

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("d.file_upload_flag = 'A'"));
        assertTrue(sql.contains("d.entry_date > COALESCE(?, '-infinity'::timestamp)"));
        assertTrue(sql.contains("m.message LIKE '%a=' || d.uniqueid || '%'"));
    }
}
