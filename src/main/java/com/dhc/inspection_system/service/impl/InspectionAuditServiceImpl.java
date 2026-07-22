package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.InspectionAuditDAO;
import com.dhc.inspection_system.service.InspectionAuditService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InspectionAuditServiceImpl implements InspectionAuditService {

    @Autowired
    private InspectionAuditDAO inspectionAuditDAO;

    @Override
    public int saveInspectionAuditLog(
            int diaryNo,
            int diaryYr,
            String description,
            String actor
    ) {
        return inspectionAuditDAO.saveInspectionAuditLog(
                diaryNo,
                diaryYr,
                description,
                actor
        );
    }
}
