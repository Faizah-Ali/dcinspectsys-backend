package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.auth.JwtUtil;
import com.dhc.inspection_system.dao.ApplicationDAO;
import com.dhc.inspection_system.dao.LoginDAO;
import com.dhc.inspection_system.dao.UploadDAO;
import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.DeleteInspectionFileRequest;
import com.dhc.inspection_system.dto.LoginUserDTO;
import com.dhc.inspection_system.dto.UploadApplicantDetails;
import com.dhc.inspection_system.service.InspectionAuditService;
import com.dhc.inspection_system.service.UploadService;
import com.dhc.inspection_system.utils.OnlineInspectionMessageHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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

    @Value("${inspection.download.public-url}")
    private String downloadPublicUrl;

    @Autowired
    private UploadDAO uploadDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private LoginDAO loginDAO;

    @Autowired
    private InspectionAuditService inspectionAuditService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
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

        LoginUserDTO loggedInUser = loginDAO.getUserByUsername(entryBy);
        String loggedInRole = loggedInUser != null ? loggedInUser.getRole() : null;

        if (!"ONLINEINSPECTION".equals(loggedInRole)) {
            throw new AccessDeniedException(
                    "Only Inspection Officer can upload inspection files."
            );
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

        ApplicationDetailsResponse applicationDetails =
                applicationDAO.getApplicationDetails(diaryNoInt, diaryYrInt);

        String currentStatus = applicationDetails != null
                ? applicationDetails.getStatus()
                : null;
        if (!"P".equals(currentStatus)) {
            throw new AccessDeniedException(
                    "Application cannot accept uploads in its current status."
            );
        }

        String assigned = applicationDetails != null
                ? applicationDetails.getAssigned()
                : null;
        if (assigned == null || !assigned.equals(entryBy)) {
            throw new AccessDeniedException(
                    "You are not authorized to process this application."
            );
        }

        List<Path> savedFiles = new ArrayList<>();
        registerFileCleanupOnRollback(savedFiles);

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

                Path savedFilePath = Paths.get(uploadDirectoryPath, fileName);
                savedFiles.add(savedFilePath);
                uploadDAO.saveInspectionFile(file, fileName);

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

                String message = OnlineInspectionMessageHelper.buildEmailMessage(
                        diaryNoInt,
                        diaryYrInt,
                        fileName,
                        uniqueId,
                        mobileNo,
                        downloadPublicUrl
                );
                String sms = OnlineInspectionMessageHelper.buildSmsMessage(
                        password,
                        diaryNoInt,
                        diaryYrInt,
                        fileName
                );

                int messageRows = uploadDAO.saveOnlineInspectionMessage(
                        diaryNoInt,
                        diaryYrInt,
                        message,
                        emailId,
                        mobileNo,
                        sms
                );

                if (messageRows <= 0) {
                    throw new RuntimeException("Failed to insert inspection_user_online_message");
                }

                String description = "PDF File named as " + fileName + " uploaded for e-Inspection";
                int logRows = inspectionAuditService.saveInspectionAuditLog(
                        diaryNoInt,
                        diaryYrInt,
                        description,
                        entryBy
                );

                if (logRows <= 0) {
                    throw new RuntimeException("Failed to insert efiling_log");
                }
            }

            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            deleteSavedFiles(savedFiles);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteInspectionFile(String authorization, DeleteInspectionFileRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        if (request.getUniqueId() == null || request.getUniqueId().isBlank()) {
            throw new IllegalArgumentException("uniqueId is required");
        }

        String loggedInUsername = extractUsernameFromAuthorization(authorization);
        if (loggedInUsername == null || loggedInUsername.isBlank()) {
            throw new IllegalArgumentException("Authorization is required");
        }

        LoginUserDTO loggedInUser = loginDAO.getUserByUsername(loggedInUsername);
        String loggedInRole = loggedInUser != null ? loggedInUser.getRole() : null;

        if (!"ONLINEINSPECTION".equals(loggedInRole)) {
            throw new AccessDeniedException(
                    "Only Inspection Officer can delete inspection files."
            );
        }

        ApplicationDetailsResponse applicationDetails =
                applicationDAO.getApplicationDetails(request.getDiaryNo(), request.getDiaryYr());

        String currentStatus = applicationDetails != null
                ? applicationDetails.getStatus()
                : null;
        if (!"P".equals(currentStatus)) {
            throw new AccessDeniedException(
                    "Application cannot accept file deletion in its current status."
            );
        }

        String assigned = applicationDetails != null
                ? applicationDetails.getAssigned()
                : null;
        if (assigned == null || !assigned.equals(loggedInUsername)) {
            throw new AccessDeniedException(
                    "You are not authorized to process this application."
            );
        }

        return uploadDAO.markFileDeleted(
                request.getUniqueId().trim(),
                request.getDiaryNo(),
                request.getDiaryYr()
        );
    }

    private void registerFileCleanupOnRollback(List<Path> savedFiles) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    deleteSavedFiles(savedFiles);
                }
            }
        });
    }

    private void deleteSavedFiles(List<Path> savedFiles) {
        for (Path path : savedFiles) {
            try {
                Files.deleteIfExists(path);
            } catch (Exception deleteException) {
                deleteException.printStackTrace();
            }
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
