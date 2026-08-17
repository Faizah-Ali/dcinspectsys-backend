package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.EmailQueueDAO;
import com.dhc.inspection_system.dto.OnlineInspectionMessageRow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

@Repository
public class EmailQueueDAOImpl implements EmailQueueDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<OnlineInspectionMessageRow> getOnlineInspectionMessages(
            int diaryNo,
            int diaryYr,
            Timestamp cycleCutoff
    ) {
        String sql = """
            SELECT m.message, m.email
            FROM judl.inspection_user_online_message m
            WHERE m.diary_no = ?
              AND m.diary_yr = ?
              AND EXISTS (
                  SELECT 1
                  FROM judl.data_share_receiver_details d
                  WHERE d.diary_no = m.diary_no
                    AND d.diary_yr = m.diary_yr
                    AND d.file_upload_flag = 'A'
                    AND d.entry_date > COALESCE(?, '-infinity'::timestamp)
                    AND m.message LIKE '%a=' || d.uniqueid || '%'
              )
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    OnlineInspectionMessageRow row = new OnlineInspectionMessageRow();
                    row.setMessage(rs.getString("message"));
                    row.setEmail(rs.getString("email"));
                    return row;
                },
                diaryNo,
                diaryYr,
                cycleCutoff
        );
    }

    @Override
    public int nextOrderId() {
        int year = Calendar.getInstance().get(Calendar.YEAR);

        String selectSql = """
            SELECT curid
            FROM judl.idgenerate
            WHERE tblname = 'ORDER'
              AND year = ?
            """;

        List<Integer> ids = jdbcTemplate.query(
                selectSql,
                (rs, rowNum) -> rs.getInt("curid"),
                year
        );

        int currentId;
        if (ids.isEmpty() || ids.get(0) == null || ids.get(0) <= 0) {
            String insertSql = """
                INSERT INTO judl.idgenerate (tblname, curid, year)
                VALUES ('ORDER', 1, ?)
                """;
            int inserted = jdbcTemplate.update(insertSql, year);
            if (inserted <= 0) {
                throw new RuntimeException("Failed to initialize idgenerate for ORDER");
            }
            currentId = 1;
        } else {
            currentId = ids.get(0);
        }

        String updateSql = """
            UPDATE judl.idgenerate
            SET curid = ?
            WHERE tblname = 'ORDER'
              AND year = ?
            """;

        int updated = jdbcTemplate.update(updateSql, currentId + 1, year);
        if (updated <= 0) {
            throw new RuntimeException("Failed to update idgenerate for ORDER");
        }

        return currentId;
    }

    @Override
    public int insertEmailOperation(String emailId, String generatedId, String subject) {
        String sql = """
            INSERT INTO judl.email_operation
            (
                dt_email,
                emailid,
                id,
                subject,
                email_sent
            )
            VALUES
            (
                CURRENT_TIMESTAMP,
                ?,
                ?,
                ?,
                'W'
            )
            """;

        return jdbcTemplate.update(sql, emailId, generatedId, subject);
    }

    @Override
    public int insertEmailMessageContent(String messageChunk, String generatedId) {
        String sql = """
            INSERT INTO judl.email_message_counter
            (
                msg,
                id
            )
            VALUES
            (
                ?,
                ?
            )
            """;

        return jdbcTemplate.update(sql, messageChunk, generatedId);
    }
}
