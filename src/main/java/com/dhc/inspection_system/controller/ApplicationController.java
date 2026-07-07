package com.dhc.inspection_system.controller;

import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.service.ApplicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.dhc.inspection_system.dto.PaginatedResponse;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/application-details")
    public ApplicationDetailsResponse getApplicationDetails(
            @RequestParam("diary_no") int diaryNo,
            @RequestParam("diary_yr") int diaryYr
    ) {
        return applicationService.getApplicationDetails(diaryNo, diaryYr);
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
}