package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.SmsQueueDAO;
import com.dhc.inspection_system.dto.OnlineInspectionSmsRow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class SmsQueueDAOImpl implements SmsQueueDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<OnlineInspectionSmsRow> getOnlineInspectionSmsMessages(
            int diaryNo,
            int diaryYr,
            Timestamp cycleCutoff
    ) {
        String sql = """
            SELECT m.sms, m.mobile
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
                    OnlineInspectionSmsRow row = new OnlineInspectionSmsRow();
                    row.setSms(rs.getString("sms"));
                    row.setMobile(rs.getString("mobile"));
                    return row;
                },
                diaryNo,
                diaryYr,
                cycleCutoff
        );
    }

    @Override
    public int insertSmsOperation(
            String message,
            String mobileNo,
            String purpose,
            String smsSentBy,
            String templateId
    ) {
        String sql = """
            INSERT INTO judl.sms_operation
            (
                msg,
                dt_sms,
                mobile_no,
                purpose,
                sms_sent_by,
                templateid,
                sms_sent
            )
            VALUES
            (
                ?,
                CURRENT_TIMESTAMP,
                ?,
                ?,
                ?,
                ?,
                'N'
            )
            """;

        return jdbcTemplate.update(
                sql,
                message,
                mobileNo,
                purpose,
                smsSentBy,
                templateId
        );
    }
}
