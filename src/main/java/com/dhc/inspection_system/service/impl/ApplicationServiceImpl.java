package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.ApplicationDAO;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.service.ApplicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.dhc.inspection_system.dto.PaginatedResponse;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationDAO applicationDAO;

    @Override
    public PaginatedResponse<ApplicationResponse> getApplications(
            String owner,
            String status,
            int page,
            int size
    ) {

        return applicationDAO.getApplications(
                owner,
                status,
                page,
                size
        );
    }
}