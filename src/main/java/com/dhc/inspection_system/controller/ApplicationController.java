package com.dhc.inspection_system.controller;

import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.AssignApplicationRequest;
import com.dhc.inspection_system.service.ApplicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dhc.inspection_system.dto.PaginatedResponse;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/application-details")
    public ResponseEntity<?> getApplicationDetails(
            @RequestParam("diary_no") int diaryNo,
            @RequestParam("diary_yr") int diaryYr
    ) {
        ApplicationDetailsResponse details =
                applicationService.getApplicationDetails(diaryNo, diaryYr);

        if (details == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("No records found.");
        }

        return ResponseEntity.ok(details);
    }

    @GetMapping("/applications")
    public PaginatedResponse<ApplicationResponse> getApplications(
            @RequestParam String owner,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String caseStatus,
            @RequestParam(required = false) String applicationStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return applicationService.getApplications(
                owner,
                status,
                search,
                caseStatus,
                applicationStatus,
                page,
                size
        );
    }

    @PatchMapping("/assign-application")
    public ResponseEntity<Map<String, String>> assignApplication(
            @RequestBody AssignApplicationRequest request
    ) {
        try {
            int updatedRows = applicationService.assignApplication(request);

            if (updatedRows > 0) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Application is Assigned successfully.");
                return ResponseEntity.ok(response);
            }

            Map<String, String> notFoundResponse = new HashMap<>();
            notFoundResponse.put("message", "No records found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred while assigning the application.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}