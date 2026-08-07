package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.InspectionAuditDAO;
import com.dhc.inspection_system.service.InspectionAuditService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    // Separate TX so log failure cannot roll back the caller (legacy uses own connection).
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int saveEfilingLog(
            int diaryNo,
            int diaryYr,
            String description,
            String actor,
            String ipAddress
    ) {
        return inspectionAuditDAO.saveEfilingLog(
                diaryNo,
                diaryYr,
                description,
                actor,
                ipAddress
        );
    }
}
