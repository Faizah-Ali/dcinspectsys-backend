package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.ApplicationDAO;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.PaginatedResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApplicationDAOImpl implements ApplicationDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public PaginatedResponse<ApplicationResponse> getApplications(
            String owner,
            String status,
            int page,
            int size
    ) {

        if (page < 1) {
            page = 1;
        }

        if (size < 1) {
            size = 20;
        }

        int offset = (page - 1) * size;

        String query = """
            SELECT
                username,
                casetype,
                reg_no,
                reg_yr,
                diary_no,
                diary_yr,
                remarks,
                ecourt_fee_id,
                case_title,
                applied_date,
                status,
                case_status,
                ecourtmessage,
                court_fee_amount,
                court_fee_reason
            FROM judl.INSPECTION_USER_ONLINE
            WHERE owner = ?
            """;

        if (status != null && !status.isBlank()) {
            query += " AND status = ? ";
        }

        query += """
            ORDER BY diary_yr DESC, diary_no DESC
            LIMIT ? OFFSET ?
            """;

        String countQuery = """
            SELECT COUNT(*)
            FROM judl.INSPECTION_USER_ONLINE
            WHERE owner = ?
            """;

        if (status != null && !status.isBlank()) {
            countQuery += " AND status = ? ";
        }

        Long totalRecords;

        if (status != null && !status.isBlank()) {

            totalRecords = jdbcTemplate.queryForObject(
                    countQuery,
                    Long.class,
                    owner,
                    status
            );

        } else {

            totalRecords = jdbcTemplate.queryForObject(
                    countQuery,
                    Long.class,
                    owner
            );
        }

        List<ApplicationResponse> applications = jdbcTemplate.query(

                query,

                status != null && !status.isBlank()
                        ? new Object[]{owner, status, size, offset}
                        : new Object[]{owner, size, offset},

                (rs, rowNum) -> {

                    ApplicationResponse obj = new ApplicationResponse();

                    obj.setUsername(rs.getString("username"));
                    obj.setCasetype(rs.getString("casetype"));
                    obj.setRegNo(rs.getInt("reg_no"));
                    obj.setRegYr(rs.getInt("reg_yr"));
                    obj.setDiaryNo(rs.getInt("diary_no"));
                    obj.setDiaryYr(rs.getInt("diary_yr"));
                    obj.setRemarks(rs.getString("remarks"));
                    obj.setEcourtFeeId(rs.getString("ecourt_fee_id"));
                    obj.setCaseTitle(rs.getString("case_title"));
                    obj.setAppliedDate(rs.getString("applied_date"));
                    obj.setStatus(rs.getString("status"));
                    obj.setCaseStatus(rs.getString("case_status"));
                    obj.setEcourtMessage(rs.getString("ecourtmessage"));
                    obj.setCourtFeeAmount(rs.getString("court_fee_amount"));
                    obj.setCourtFeeReason(rs.getString("court_fee_reason"));

                    return obj;
                }
        );

        PaginatedResponse<ApplicationResponse> response =
                new PaginatedResponse<>();

        response.setContent(applications);
        response.setPage(page);
        response.setSize(size);
        response.setTotalRecords(totalRecords);

        int totalPages = (int) Math.ceil(
                (double) totalRecords / size
        );

        response.setTotalPages(totalPages);

        response.setHasNext(page < totalPages);
        response.setHasPrevious(page > 1);

        return response;
    }
}