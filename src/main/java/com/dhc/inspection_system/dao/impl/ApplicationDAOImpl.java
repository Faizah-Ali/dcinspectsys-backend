package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.ApplicationDAO;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.PaginatedResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ApplicationDAOImpl implements ApplicationDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public PaginatedResponse<ApplicationResponse> getApplications(
            String owner,
            String status,
            String search,
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

        boolean hasStatus = status != null && !status.isBlank();
        boolean hasSearch = search != null && !search.isBlank();

        // Build the WHERE clause once and reuse it for both data and count queries
        // so that totalRecords reflects the same filters (owner + status + search).
        StringBuilder whereClause = new StringBuilder(" WHERE owner = ? ");
        List<Object> filterParams = new ArrayList<>();
        filterParams.add(owner);

        if (hasStatus) {
            whereClause.append(" AND status = ? ");
            filterParams.add(status);
        }

        if (hasSearch) {
            // Case-insensitive LIKE for text fields, CAST(... AS TEXT) for numeric
            // fields. applied_date is also cast to TEXT so this works whether the
            // column is DATE/TIMESTAMP or already textual.
            whereClause.append(
                    " AND ( "
                            + " LOWER(username) LIKE LOWER(?) "
                            + " OR LOWER(casetype) LIKE LOWER(?) "
                            + " OR CAST(reg_no AS TEXT) LIKE ? "
                            + " OR CAST(reg_yr AS TEXT) LIKE ? "
                            + " OR CAST(diary_no AS TEXT) LIKE ? "
                            + " OR CAST(diary_yr AS TEXT) LIKE ? "
                            + " OR LOWER(case_title) LIKE LOWER(?) "
                            + " OR LOWER(ecourt_fee_id) LIKE LOWER(?) "
                            + " OR LOWER(CAST(applied_date AS TEXT)) LIKE LOWER(?) "
                            + " OR LOWER(status) LIKE LOWER(?) "
                            + " OR LOWER(case_status) LIKE LOWER(?) "
                            + " ) "
            );

            String like = "%" + search.trim() + "%";
            // 11 placeholders -> 11 bound values, all parameterized (no concat).
            for (int i = 0; i < 11; i++) {
                filterParams.add(like);
            }
        }

        String dataQuery = """
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
            """
                + whereClause
                + """
            ORDER BY diary_yr DESC, diary_no DESC
            LIMIT ? OFFSET ?
            """;

        String countQuery = """
            SELECT COUNT(*)
            FROM judl.INSPECTION_USER_ONLINE
            """
                + whereClause;

        // Count uses only filter params.
        Long totalRecords = jdbcTemplate.queryForObject(
                countQuery,
                Long.class,
                filterParams.toArray()
        );

        if (totalRecords == null) {
            totalRecords = 0L;
        }

        // Data uses filter params + LIMIT + OFFSET.
        List<Object> dataParams = new ArrayList<>(filterParams);
        dataParams.add(size);
        dataParams.add(offset);

        List<ApplicationResponse> applications = jdbcTemplate.query(

                dataQuery,

                dataParams.toArray(),

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
