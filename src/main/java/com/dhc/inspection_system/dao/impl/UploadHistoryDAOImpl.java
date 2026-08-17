package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.UploadHistoryDAO;
import com.dhc.inspection_system.dto.InspectionLogResponse;
import com.dhc.inspection_system.dto.UploadHistoryResponse;
import com.dhc.inspection_system.dto.UserCommentResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class UploadHistoryDAOImpl implements UploadHistoryDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<UploadHistoryResponse> getUploadHistory(int diaryNo, int diaryYr) {
        String sql = """
            SELECT
                d.uniqueid,
                d.email_id,
                d.file_name,
                COALESCE(d.diary_no,0) AS diary_no,
                COALESCE(d.diary_yr,0) AS diary_yr,
                d.mobile_no,
                d.entry_date,
                COALESCE(d.entry_by,'') AS entry_by,
                COALESCE(d.file_upload_flag, 'A') AS file_upload_flag,
                CASE
                    WHEN COALESCE(d.file_upload_flag, 'A') = 'A'
                     AND (
                         i.reject_complete_date IS NULL
                         OR d.entry_date > i.reject_complete_date
                     )
                    THEN TRUE
                    ELSE FALSE
                END AS current_cycle
            FROM judl.data_share_receiver_details d
            LEFT JOIN judl.inspection_user_online i
              ON i.diary_no = d.diary_no
             AND i.diary_yr = d.diary_yr
            WHERE d.diary_no = ?
              AND d.diary_yr = ?
            ORDER BY d.entry_date DESC
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    UploadHistoryResponse obj = new UploadHistoryResponse();
                    obj.setUniqueId(rs.getString("uniqueid"));
                    obj.setEmailId(rs.getString("email_id"));
                    obj.setFileName(rs.getString("file_name"));
                    obj.setDiaryNo(rs.getLong("diary_no"));
                    obj.setDiaryYr(rs.getInt("diary_yr"));
                    obj.setMobileNo(rs.getString("mobile_no"));

                    Timestamp entryDate = rs.getTimestamp("entry_date");
                    if (entryDate != null) {
                        obj.setEntryDate(entryDate.toLocalDateTime());
                    }

                    obj.setEntryBy(rs.getString("entry_by"));
                    obj.setFileUploadFlag(rs.getString("file_upload_flag"));
                    obj.setCurrentCycle(rs.getBoolean("current_cycle"));
                    return obj;
                },
                diaryNo,
                diaryYr
        );
    }

    @Override
    public List<InspectionLogResponse> getInspectionLogs(Integer diaryNo, Integer diaryYr) {
        String sql = """
            SELECT
                entry_date,
                description,
                actor
            FROM judl.efiling_log
            WHERE source = 'e-Inspection'
              AND diaryno = ?
              AND diary_yr = ?
            ORDER BY entry_date DESC
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    InspectionLogResponse obj = new InspectionLogResponse();

                    Timestamp entryDate = rs.getTimestamp("entry_date");
                    if (entryDate != null) {
                        obj.setEntryDate(entryDate.toLocalDateTime());
                    }

                    obj.setDescription(rs.getString("description"));
                    obj.setActor(rs.getString("actor"));
                    return obj;
                },
                diaryNo,
                diaryYr
        );
    }

    @Override
    public List<UserCommentResponse> getUserComments(Integer diaryNo, Integer diaryYr) {
        String sql = """
            SELECT
                D.content,
                P.emp_name AS author,
                D.commentposting_dt
            FROM judl.dropbox_comment D
            INNER JOIN access.pis_employees P
                ON D.author = P.emp_code
            WHERE D.item_id = ?
            ORDER BY D.commentposting_dt DESC
            """;

        String itemId = diaryNo + "_" + diaryYr;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    UserCommentResponse obj = new UserCommentResponse();
                    obj.setContent(rs.getString("content"));
                    obj.setAuthor(rs.getString("author"));
                    obj.setCommentDate(rs.getString("commentposting_dt"));
                    return obj;
                },
                itemId
        );
    }

    @Override
    public int saveOfficeComment(Integer diaryNo, Integer diaryYr, String content, String author) {
        String sql = """
            INSERT INTO judl.dropbox_comment
            (
                item_id,
                content,
                author,
                commentposting_dt,
                document_id,
                rowuniqueid
            )
            VALUES
            (
                ?,
                ?,
                ?,
                CURRENT_TIMESTAMP,
                NULL,
                ?
            )
            """;

        String itemId = diaryNo + "_" + diaryYr;

        return jdbcTemplate.update(sql, itemId, content, author, itemId);
    }

}
