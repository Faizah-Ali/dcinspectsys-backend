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
                uniqueid,
                email_id,
                file_name,
                COALESCE(diary_no,0) AS diary_no,
                COALESCE(diary_yr,0) AS diary_yr,
                mobile_no,
                entry_date,
                COALESCE(entry_by,'') AS entry_by
            FROM judl.data_share_receiver_details
            WHERE diary_no = ?
              AND diary_yr = ?
            ORDER BY entry_date DESC
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
                content,
                author,
                commentposting_dt
            FROM judl.dropbox_comment
            WHERE item_id = ?
            ORDER BY commentposting_dt
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

}
