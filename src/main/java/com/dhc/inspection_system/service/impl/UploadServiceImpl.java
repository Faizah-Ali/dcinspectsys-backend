package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.auth.JwtUtil;
import com.dhc.inspection_system.dao.UploadDAO;
import com.dhc.inspection_system.dto.UploadApplicantDetails;
import com.dhc.inspection_system.service.UploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    @Value("${inspection.upload.path}")
    private String uploadDirectoryPath;

    @Autowired
    private UploadDAO uploadDAO;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public int uploadInspectionFiles(
            MultipartFile[] files,
            String diaryNo,
            String diaryYr,
            String authorization
    ) {
        if (diaryNo == null || diaryNo.isBlank()) {
            throw new IllegalArgumentException("diaryNo is required");
        }

        if (diaryYr == null || diaryYr.isBlank()) {
            throw new IllegalArgumentException("diaryYr is required");
        }

        String entryBy = extractUsernameFromAuthorization(authorization);
        if (entryBy == null || entryBy.isBlank()) {
            throw new IllegalArgumentException("Authorization is required");
        }

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("files is required");
        }

        for (MultipartFile file : files) {
            validatePdfFile(file);
        }

        String trimmedDiaryNo = diaryNo.trim();
        String trimmedDiaryYr = diaryYr.trim();

        int diaryNoInt;
        int diaryYrInt;
        try {
            diaryNoInt = Integer.parseInt(trimmedDiaryNo);
            diaryYrInt = Integer.parseInt(trimmedDiaryYr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("diaryNo and diaryYr must be valid numbers");
        }

        List<Path> savedFiles = new ArrayList<>();

        try {
            UploadApplicantDetails applicant = uploadDAO.getApplicantDetails(diaryNoInt, diaryYrInt);

            String name = applicant != null ? applicant.getName() : null;
            String mobileNo = applicant != null ? applicant.getMobileNo() : null;
            String emailId = applicant != null ? applicant.getEmailId() : null;
            String loginId = applicant != null ? applicant.getLoginId() : null;
            String password = applicant != null ? applicant.getPassword() : null;

            for (MultipartFile file : files) {
                String fileName = trimmedDiaryNo + "-" + trimmedDiaryYr + "-"
                        + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
                String uniqueId = UUID.randomUUID().toString();

                uploadDAO.saveInspectionFile(file, fileName);
                Path savedFilePath = Paths.get(uploadDirectoryPath, fileName);
                savedFiles.add(savedFilePath);

                int insertedRows = uploadDAO.insertDataShareReceiverDetails(
                        name,
                        mobileNo,
                        emailId,
                        entryBy,
                        fileName,
                        loginId,
                        password,
                        diaryNoInt,
                        diaryYrInt,
                        uniqueId
                );

                if (insertedRows <= 0) {
                    throw new RuntimeException("Failed to insert upload metadata into data_share_receiver_details");
                }
            }

            return files.length;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            for (Path path : savedFiles) {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception deleteException) {
                    deleteException.printStackTrace();
                }
            }
            throw e;
        }
    }

    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
    }

    private String extractUsernameFromAuthorization(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        // Service removes "Bearer " prefix.
        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim()
                : authorization.trim();

        if (token.isBlank() || !jwtUtil.validateToken(token)) {
            return null;
        }

        return jwtUtil.extractUsername(token);
    }

}
