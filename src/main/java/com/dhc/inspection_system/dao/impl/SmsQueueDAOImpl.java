package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.SmsQueueDAO;
import com.dhc.inspection_system.dto.OnlineInspectionSmsRow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SmsQueueDAOImpl implements SmsQueueDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<OnlineInspectionSmsRow> getOnlineInspectionSmsMessages(int diaryNo, int diaryYr) {
        String sql = """
            SELECT sms, mobile
            FROM judl.inspection_user_online_message
            WHERE diary_no = ?
              AND diary_yr = ?
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
                diaryYr
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
