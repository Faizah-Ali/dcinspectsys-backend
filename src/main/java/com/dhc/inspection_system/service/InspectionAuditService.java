package com.dhc.inspection_system.service;

public interface InspectionAuditService {

    int saveInspectionAuditLog(
            int diaryNo,
            int diaryYr,
            String description,
            String actor
    );

    int saveEfilingLog(
            int diaryNo,
            int diaryYr,
            String description,
            String actor,
            String ipAddress
    );
}
