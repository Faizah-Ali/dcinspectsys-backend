package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.UploadDAO;
import com.dhc.inspection_system.dto.UploadApplicantDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Repository
public class UploadDAOImpl implements UploadDAO {

    @Value("${inspection.upload.path}")
    private String uploadDirectoryPath;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void saveInspectionFile(MultipartFile file, String fileName) {
        try {
            Path uploadDirectory = Paths.get(uploadDirectoryPath);
            Files.createDirectories(uploadDirectory);

            Path destination = uploadDirectory.resolve(fileName);

            file.transferTo(destination.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public UploadApplicantDetails getApplicantDetails(int diaryNo, int diaryYr) {
        String sql = """
            SELECT username, mobileno, email, userid, password
            FROM judl.inspection_user_online
            WHERE diary_no = ?
              AND diary_yr = ?
            """;

        try {
            List<UploadApplicantDetails> results = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> {
                        UploadApplicantDetails details = new UploadApplicantDetails();
                        details.setName(rs.getString("username"));
                        details.setMobileNo(rs.getString("mobileno"));
                        details.setEmailId(rs.getString("email"));
                        details.setLoginId(rs.getString("userid"));
                        details.setPassword(rs.getString("password"));
                        return details;
                    },
                    diaryNo,
                    diaryYr
            );

            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public int insertDataShareReceiverDetails(
            String name,
            String mobileNo,
            String emailId,
            String entryBy,
            String fileName,
            String loginId,
            String password,
            int diaryNo,
            int diaryYr,
            String uniqueId
    ) {
        String sql = """
            INSERT INTO judl.data_share_receiver_details
            (NAME, MOBILE_NO, EMAIL_ID, ENTRY_BY, FILE_NAME, LOGIN_ID, PASSWORD, DIARY_NO, DIARY_YR, UNIQUEID, FILE_UPLOAD_FLAG)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'A')
            """;

        try {
            return jdbcTemplate.update(
                    sql,
                    name,
                    mobileNo,
                    emailId,
                    entryBy,
                    fileName,
                    loginId,
                    password,
                    diaryNo,
                    diaryYr,
                    uniqueId
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public int saveOnlineInspectionMessage(
            int diaryNo,
            int diaryYr,
            String message,
            String email,
            String mobile,
            String sms
    ) {
        String sql = """
            INSERT INTO judl.inspection_user_online_message
            (diary_no, diary_yr, message, email, mobile, sms)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        return jdbcTemplate.update(
                sql,
                diaryNo,
                diaryYr,
                message,
                email,
                mobile,
                sms
        );
    }

    @Override
    public int markFileDeleted(String uniqueId, int diaryNo, int diaryYr) {
        String sql = """
            UPDATE judl.data_share_receiver_details
            SET file_upload_flag = 'D'
            WHERE uniqueid = ?
              AND diary_no = ?
              AND diary_yr = ?
              AND file_upload_flag = 'A'
            """;

        return jdbcTemplate.update(sql, uniqueId, diaryNo, diaryYr);
    }

}
