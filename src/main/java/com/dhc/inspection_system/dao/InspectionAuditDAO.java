package com.dhc.inspection_system.dao;

public interface InspectionAuditDAO {

    int saveInspectionAuditLog(
            int diaryNo,
            int diaryYr,
            String description,
            String actor
    );
}
