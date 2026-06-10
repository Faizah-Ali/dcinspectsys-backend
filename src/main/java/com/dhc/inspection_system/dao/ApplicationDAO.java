package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.ApplicationResponse;

import java.util.List;
import com.dhc.inspection_system.dto.PaginatedResponse;import com.dhc.inspection_system.dto.PaginatedResponse;
public interface ApplicationDAO {

    PaginatedResponse<ApplicationResponse> getApplications(
            String owner,
            String status,
            int page,
            int size
    );
}