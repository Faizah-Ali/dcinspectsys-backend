package com.dhc.inspection_system.dao.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadDAOImplTest {

    private static final String UNIQUE_ID = "ipcgjvojbbybjdigtdcqgUXIAYTY6042167";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DownloadDAOImpl downloadDAO;

    @SuppressWarnings("unchecked")
    @Test
    void timedDownloadKeepsSixDayRestriction() {
        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                eq(UNIQUE_ID)
        )).thenReturn(Collections.emptyList());

        downloadDAO.findFileNameByUniqueId(UNIQUE_ID);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                any(RowMapper.class),
                eq(UNIQUE_ID)
        );

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("EXTRACT(DAY FROM CURRENT_TIMESTAMP - entry_date) < 6"));
        assertFalse(sql.contains("file_upload_flag"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void historyDownloadHasNoSixDayRestrictionAndIncludesDeleted() {
        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                eq(UNIQUE_ID)
        )).thenReturn(List.of("40427-2026.pdf"));

        Optional<String> fileName = downloadDAO.findHistoryFileNameByUniqueId(UNIQUE_ID);

        assertEquals(Optional.of("40427-2026.pdf"), fileName);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                any(RowMapper.class),
                eq(UNIQUE_ID)
        );

        String sql = sqlCaptor.getValue();
        assertFalse(sql.contains("EXTRACT(DAY"));
        assertFalse(sql.contains("file_upload_flag"));
        assertTrue(sql.contains("uniqueid = ?"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void historyDownloadReturnsEmptyForUnknownUniqueId() {
        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                eq("missing-id")
        )).thenReturn(Collections.emptyList());

        assertTrue(downloadDAO.findHistoryFileNameByUniqueId("missing-id").isEmpty());
    }
}
