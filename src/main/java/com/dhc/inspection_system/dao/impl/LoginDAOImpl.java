package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.LoginDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoginDAOImpl implements LoginDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String getPasswordByUsername(String username) {

        String query = """
    SELECT E.PASS
    FROM access.PIS_EMPLOYEES E
    INNER JOIN judl.dropbox_user_authorization D
        ON E.EMP_CODE = D.ID
    WHERE
        (
            D.ROLE = 'ONLINEINSPECTION'
            OR D.ROLE = 'INSPECTIONADMIN'
            OR D.ROLE = 'INSPECTIONAPPROVER'
        )
        AND E.EMP_CODE = ?
    """;
        try {

            return jdbcTemplate.queryForObject(
                    query,
                    String.class,
                    username
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}