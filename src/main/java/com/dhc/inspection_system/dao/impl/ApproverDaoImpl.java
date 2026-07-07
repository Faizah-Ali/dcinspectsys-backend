package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.ApproverDao;
import com.dhc.inspection_system.dto.ApproverResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApproverDaoImpl implements ApproverDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<ApproverResponse> getApproversList() {
        String sql = """
                SELECT id, fullname, role
                FROM judl.dropbox_user_authorization
                WHERE UPPER(role) = UPPER(?)
                ORDER BY id ASC, role ASC
                """;

        return jdbcTemplate.query(
                sql,
                new Object[]{"INSPECTIONAPPROVER"},
                (rs, rowNum) -> {
                    ApproverResponse obj = new ApproverResponse();
                    obj.setId(rs.getString("id"));
                    obj.setFullname(rs.getString("fullname"));
                    obj.setRole(rs.getString("role"));
                    return obj;
                }
        );
    }
}
