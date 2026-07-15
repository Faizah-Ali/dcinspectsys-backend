package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.LoginDAO;
import com.dhc.inspection_system.dto.LoginUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoginDAOImpl implements LoginDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public LoginUserDTO getUserByUsername(String username) {

        String query = """
    SELECT E.PASS, D.ROLE, D.branch_id
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
                    (rs, rowNum) -> {
                        LoginUserDTO dto = new LoginUserDTO();
                        dto.setPassword(rs.getString("PASS"));
                        dto.setRole(rs.getString("ROLE"));
                        dto.setGroup(rs.getString("branch_id"));
                        return dto;
                    },
                    username
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
