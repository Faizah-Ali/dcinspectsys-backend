package com.dhc.inspection_system.controller;

import com.dhc.inspection_system.dto.DeleteInspectionFileRequest;
import com.dhc.inspection_system.service.UploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping(value = "/upload-inspection-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadInspectionFile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("diaryNo") String diaryNo,
            @RequestParam("diaryYr") String diaryYr
    ) {
        try {
            int uploadedCount = uploadService.uploadInspectionFiles(files, diaryNo, diaryYr, authorization);

            Map<String, String> response = new HashMap<>();
            String documentLabel = uploadedCount == 1 ? "document" : "documents";
            response.put("message", uploadedCount + " " + documentLabel + " uploaded successfully.");
            return ResponseEntity.ok(response);

        } catch (AccessDeniedException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            // TEMP debug: expose exception details to identify the failing line
            errorResponse.put("message", e.getClass().getName() + ": " + e.getMessage());
            if (e.getCause() != null) {
                errorResponse.put(
                        "cause",
                        e.getCause().getClass().getName() + ": " + e.getCause().getMessage()
                );
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PatchMapping("/delete-inspection-file")
    public ResponseEntity<Map<String, String>> deleteInspectionFile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody DeleteInspectionFileRequest request
    ) {
        try {
            int updatedRows = uploadService.deleteInspectionFile(authorization, request);

            if (updatedRows > 0) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Inspection file deleted successfully.");
                return ResponseEntity.ok(response);
            }

            Map<String, String> notFoundResponse = new HashMap<>();
            notFoundResponse.put("message", "No records found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);

        } catch (AccessDeniedException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred while deleting the inspection file.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
