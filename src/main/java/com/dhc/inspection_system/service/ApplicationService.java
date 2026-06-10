package com.dhc.inspection_system.service;

import com.dhc.inspection_system.dto.ApplicationResponse;

import java.util.List;

import com.dhc.inspection_system.dto.PaginatedResponse;

public interface ApplicationService {

    PaginatedResponse<ApplicationResponse> getApplications(
            String owner,
            String status,
            String search,
            int page,
            int size
    );
}