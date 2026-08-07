package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.InspectionAuditDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InspectionAuditDAOImpl implements InspectionAuditDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int saveInspectionAuditLog(
            int diaryNo,
            int diaryYr,
            String description,
            String actor
    ) {
        return saveEfilingLog(diaryNo, diaryYr, description, actor, null);
    }

    @Override
    public int saveEfilingLog(
            int diaryNo,
            int diaryYr,
            String description,
            String actor,
            String ipAddress
    ) {
        String sql = """
            INSERT INTO judl.EFILING_LOG
            (
                DIARYNO,
                DIARY_YR,
                SOURCE,
                DESCRIPTION,
                ACTOR,
                ENTRY_DATE,
                IP
            )
            VALUES
            (
                ?,
                ?,
                'e-Inspection',
                ?,
                ?,
                CURRENT_TIMESTAMP,
                ?
            )
            """;

        return jdbcTemplate.update(
                sql,
                diaryNo,
                diaryYr,
                description,
                actor,
                ipAddress
        );
    }
}
