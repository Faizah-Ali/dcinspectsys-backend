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
        String sql = """
            INSERT INTO judl.efiling_log
            (
                diaryno,
                diary_yr,
                entry_date,
                source,
                description,
                actor
            )
            VALUES
            (
                ?,
                ?,
                CURRENT_TIMESTAMP,
                'e-Inspection',
                ?,
                ?
            )
            """;

        return jdbcTemplate.update(
                sql,
                diaryNo,
                diaryYr,
                description,
                actor
        );
    }
}
