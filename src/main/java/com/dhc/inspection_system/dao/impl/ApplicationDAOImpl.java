package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.ApplicationDAO;
import com.dhc.inspection_system.dao.ApplicationOrderMode;
import com.dhc.inspection_system.dto.ApplicationDetailsResponse;
import com.dhc.inspection_system.dto.ApplicationOwnershipInfo;
import com.dhc.inspection_system.dto.ApplicationResponse;
import com.dhc.inspection_system.dto.AssignApplicationRequest;
import com.dhc.inspection_system.dto.CourtFeeQueryResult;
import com.dhc.inspection_system.dto.ForwardApplicationRequest;
import com.dhc.inspection_system.dto.PaginatedResponse;
import com.dhc.inspection_system.dto.SendForApprovalRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ApplicationDAOImpl implements ApplicationDAO {

    private static final Logger log = LoggerFactory.getLogger(ApplicationDAOImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public ApplicationDetailsResponse getApplicationDetails(int diaryNo, int diaryYr) {
        String sql = """
            SELECT *
            FROM judl.INSPECTION_USER_ONLINE
            WHERE diary_no = ?
            AND diary_yr = ?
            """;

        List<ApplicationDetailsResponse> results = jdbcTemplate.query(
                sql,
                new Object[]{diaryNo, diaryYr},
                (rs, rowNum) -> {
                    ApplicationDetailsResponse obj = new ApplicationDetailsResponse();

                    // obj.setUserId(rs.getString("userid"));
                    // obj.setPassword(rs.getString("password"));
                    obj.setCasetype(rs.getString("casetype"));
                    obj.setRegNo(rs.getString("reg_no"));
                    obj.setInspectionDate(rs.getString("inspection_date"));
                    obj.setRegYr(rs.getString("reg_yr"));
                    obj.setDecidedDate(rs.getString("decided_date"));
                    obj.setCouncilFor(rs.getString("council_for"));
                    obj.setAddress(rs.getString("address"));
                    obj.setAppliedDate(rs.getString("applied_date"));
                    obj.setRole(rs.getString("role"));
                    obj.setUsername(rs.getString("username"));
                    obj.setDecision(rs.getString("decision"));
                    obj.setOwner(rs.getString("owner"));
                    obj.setDiaryNo(rs.getInt("diary_no"));
                    obj.setDiaryYr(rs.getInt("diary_yr"));
                    obj.setEcourtFeeId(rs.getString("ecourt_fee_id"));
                    obj.setIsMigrated(rs.getString("is_migrated"));
                    obj.setOnlineMode(rs.getString("online_mode"));
                    obj.setStatus(rs.getString("status"));
                    obj.setRemarks(rs.getString("remarks"));
                    obj.setEmail(rs.getString("email"));
                    obj.setCourtFeeAmount(rs.getString("court_fee_amount"));
                    obj.setIsCourtfeeLocked(rs.getString("is_courtfee_locked"));
                    obj.setCourtFeeReason(rs.getString("court_fee_reason"));
                    obj.setIsNew(rs.getString("is_new"));
                    obj.setCaseTitle(rs.getString("case_title"));
                    obj.setApplappby(rs.getString("applappby"));
                    obj.setAssigned(rs.getString("assigned"));
                    obj.setApplappbyname(rs.getString("applappbyname"));
                    obj.setAssignedname(rs.getString("assignedname"));
                    // obj.setMobileno(rs.getString("mobileno"));
                    obj.setEcourtMessage(rs.getString("ecourtmessage"));
                    obj.setOrgAppliedDate(rs.getString("org_applied_date"));
                    obj.setRejectCompleteDate(rs.getString("reject_complete_date"));
                    obj.setCaseStatus(rs.getString("case_status"));
                    obj.setRegTable(rs.getString("reg_table"));

                    return obj;
                }
        );

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public PaginatedResponse<ApplicationResponse> getApplications(
            String owner,
            String assigned,
            String applappby,
            List<String> statuses,
            String search,
            String caseStatus,
            String applicationStatus,
            boolean unassignedOnly,
            int page,
            int size,
            ApplicationOrderMode orderMode
    ) {

        if (page < 1) {
            page = 1;
        }

        if (size < 1) {
            size = 20;
        }

        int offset = (page - 1) * size;

        boolean hasOwner = owner != null && !owner.isBlank();
        boolean hasAssigned = assigned != null && !assigned.isBlank();
        boolean hasApplAppBy = applappby != null && !applappby.isBlank();
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCaseStatus = caseStatus != null && !caseStatus.isBlank();
        boolean hasApplicationStatus = applicationStatus != null && !applicationStatus.isBlank();

        List<String> statusValues = new ArrayList<>();
        if (statuses != null) {
            for (String s : statuses) {
                if (s != null && !s.isBlank()) {
                    statusValues.add(s);
                }
            }
        }
        boolean hasStatuses = !statusValues.isEmpty();

        // Build the WHERE clause once and reuse it for both data and count queries
        // so that totalRecords reflects the same filters.
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        List<Object> filterParams = new ArrayList<>();

        if (hasOwner) {
            whereClause.append(" AND owner = ? ");
            filterParams.add(owner);
        }

        if (hasAssigned) {
            whereClause.append(" AND assigned = ? ");
            filterParams.add(assigned);
        }

        if (hasApplAppBy) {
            whereClause.append(" AND applappby = ? ");
            filterParams.add(applappby);
        }

        if (hasStatuses) {
            whereClause.append(" AND status IN (");
            for (int i = 0; i < statusValues.size(); i++) {
                if (i > 0) {
                    whereClause.append(", ");
                }
                whereClause.append("?");
                filterParams.add(statusValues.get(i));
            }
            whereClause.append(") ");
        }

        if (hasCaseStatus) {
            whereClause.append(" AND case_status = ? ");
            filterParams.add(caseStatus);
        }

        if (hasApplicationStatus) {
            whereClause.append(" AND status = ? ");
            filterParams.add(applicationStatus);
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
                            + " OR LOWER(remarks) LIKE LOWER(?) "
                            + " OR LOWER(CAST(applied_date AS TEXT)) LIKE LOWER(?) "
                            + " OR LOWER(status) LIKE LOWER(?) "
                            + " OR LOWER(case_status) LIKE LOWER(?) "
                            + " ) "
            );

            String like = "%" + search.trim() + "%";
            // 12 placeholders -> 12 bound values, all parameterized (no concat).
            for (int i = 0; i < 12; i++) {
                filterParams.add(like);
            }
        }

        if (unassignedOnly) {
            whereClause.append("""
                    AND (
                        (assigned IS NULL OR TRIM(assigned) = '')
                        AND
                        (assignedname IS NULL OR TRIM(assignedname) = '')
                    )
                    """);
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
                court_fee_reason,
                assigned,
                assignedname
            FROM judl.INSPECTION_USER_ONLINE
            """
                + whereClause
                + "\n            ORDER BY " + resolveOrderBy(orderMode) + "\n            LIMIT ? OFFSET ?\n            ";

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
                    obj.setAssigned(rs.getString("assigned"));
                    obj.setAssignedname(rs.getString("assignedname"));

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

    @Override
    public int assignApplication(AssignApplicationRequest request) {
        String sql = """
            UPDATE judl.inspection_user_online
            SET
                status = 'N',
                assigned = ?,
                assignedname = ?,
                remarks = COALESCE(NULLIF(TRIM(?), ''), remarks),
                reject_complete_date = CURRENT_TIMESTAMP
            WHERE diary_no = ?
              AND diary_yr = ?
            """;

        return jdbcTemplate.update(
                sql,
                request.getAssigned(),
                request.getAssignedname(),
                request.getRemarks(),
                request.getDiaryNo(),
                request.getDiaryYr()
        );
    }

    @Override
    public int approveApplication(int diaryNo, int diaryYr, String remarks) {
        String sql = """
            UPDATE judl.inspection_user_online
            SET
                status='P',
                remarks = COALESCE(NULLIF(TRIM(?), ''), remarks)
            WHERE diary_no=?
            AND diary_yr=?
            """;

        return jdbcTemplate.update(sql, remarks, diaryNo, diaryYr);
    }

    @Override
    public int rejectApplication(int diaryNo, int diaryYr, String remarks) {
        String sql = """
            UPDATE judl.inspection_user_online
            SET
                status='K',
                remarks = COALESCE(NULLIF(TRIM(?), ''), remarks)
            WHERE diary_no=?
            AND diary_yr=?
            """;

        return jdbcTemplate.update(sql, remarks, diaryNo, diaryYr);
    }

    @Override
    public int rejectApplicationByOfficer(int diaryNo, int diaryYr, String remarks) {
        String sql = """
            UPDATE judl.inspection_user_online
            SET
                status='C',
                remarks = COALESCE(NULLIF(TRIM(?), ''), remarks),
                reject_complete_date=CURRENT_TIMESTAMP
            WHERE diary_no=?
            AND diary_yr=?
            """;

        return jdbcTemplate.update(sql, remarks, diaryNo, diaryYr);
    }

    @Override
    public int sendForApproval(
            SendForApprovalRequest request,
            String officerUsername,
            String officerFullName
    ) {
        String sql = """
            UPDATE judl.inspection_user_online
            SET
                status='T',
                applappby=?,
                applappbyname=?,
                assigned=?,
                assignedname=?,
                remarks = COALESCE(NULLIF(TRIM(?), ''), remarks)
            WHERE diary_no=?
            AND diary_yr=?
            """;

        return jdbcTemplate.update(
                sql,
                request.getApproverId(),
                request.getApproverName(),
                officerUsername,
                officerFullName,
                request.getRemarks(),
                request.getDiaryNo(),
                request.getDiaryYr()
        );
    }

    @Override
    public int forwardApplication(ForwardApplicationRequest request) {
        String sql = """
            UPDATE judl.inspection_user_online
            SET
                applappby=?,
                applappbyname=?
            WHERE diary_no=?
            AND diary_yr=?
            """;

        return jdbcTemplate.update(
                sql,
                request.getApproverId(),
                request.getApproverName(),
                request.getDiaryNo(),
                request.getDiaryYr()
        );
    }

    @Override
    public int completeApplication(int diaryNo, int diaryYr, String remarks) {
        String sql = """
            UPDATE judl.inspection_user_online
            SET
                status='Y',
                remarks = COALESCE(NULLIF(TRIM(?), ''), remarks),
                reject_complete_date=CURRENT_TIMESTAMP,
                is_courtfee_locked='Y'
            WHERE diary_no=?
            AND diary_yr=?
            """;

        return jdbcTemplate.update(sql, remarks, diaryNo, diaryYr);
    }

    @Override
    public boolean updateCourtFee(int diaryNo, int diaryYr, CourtFeeQueryResult result) {
        String courtFeeAmount;
        String isCourtfeeLocked;
        String ecourtMessage;

        if (result != null && result.isSuccess()) {
            courtFeeAmount = result.getAmount() == null ? "" : result.getAmount();
            isCourtfeeLocked = "N";
            ecourtMessage = "VALID COURT FEE";
        } else {
            courtFeeAmount = "";
            isCourtfeeLocked = "";
            String message = (result == null || result.getMessage() == null)
                    ? ""
                    : result.getMessage();
            ecourtMessage = "Error in court fee:" + message;
        }

        String sql = """
            UPDATE judl.inspection_user_online
            SET
                court_fee_amount = ?,
                is_courtfee_locked = ?,
                ecourtmessage = ?
            WHERE diary_no = ?
              AND diary_yr = ?
            """;

        int updatedRows = jdbcTemplate.update(
                sql,
                courtFeeAmount,
                isCourtfeeLocked,
                ecourtMessage,
                diaryNo,
                diaryYr
        );

        boolean updated = updatedRows == 1;
        if (updated) {
            log.info(
                    "Updated court fee for diaryNo={}, diaryYr={}, success={}",
                    diaryNo,
                    diaryYr,
                    result != null && result.isSuccess()
            );
        } else {
            log.warn(
                    "Court fee update affected {} row(s) for diaryNo={}, diaryYr={}",
                    updatedRows,
                    diaryNo,
                    diaryYr
            );
        }

        return updated;
    }

    @Override
    public Timestamp getCycleCutoff(int diaryNo, int diaryYr) {
        String sql = """
            SELECT reject_complete_date
            FROM judl.inspection_user_online
            WHERE diary_no = ?
              AND diary_yr = ?
            """;

        List<Timestamp> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getTimestamp("reject_complete_date"),
                diaryNo,
                diaryYr
        );

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public boolean hasDataShareReceiverDetails(
            int diaryNo,
            int diaryYr,
            Timestamp cycleCutoff
    ) {
        String sql = """
            SELECT COUNT(1)
            FROM judl.data_share_receiver_details d
            WHERE d.diary_no = ?
              AND d.diary_yr = ?
              AND d.file_upload_flag = 'A'
              AND d.entry_date > COALESCE(?, '-infinity'::timestamp)
            """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                diaryNo,
                diaryYr,
                cycleCutoff
        );
        return count != null && count > 0;
    }

    @Override
    public boolean hasOnlineInspectionMessage(
            int diaryNo,
            int diaryYr,
            Timestamp cycleCutoff
    ) {
        String sql = """
            SELECT COUNT(1)
            FROM judl.inspection_user_online_message m
            WHERE m.diary_no = ?
              AND m.diary_yr = ?
              AND EXISTS (
                  SELECT 1
                  FROM judl.data_share_receiver_details d
                  WHERE d.diary_no = m.diary_no
                    AND d.diary_yr = m.diary_yr
                    AND d.file_upload_flag = 'A'
                    AND d.entry_date > COALESCE(?, '-infinity'::timestamp)
                    AND m.message LIKE '%a=' || d.uniqueid || '%'
              )
            """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                diaryNo,
                diaryYr,
                cycleCutoff
        );
        return count != null && count > 0;
    }

    @Override
    public ApplicationOwnershipInfo getStatusAndApplappby(int diaryNo, int diaryYr) {
        String sql = """
            SELECT status, applappby
            FROM judl.inspection_user_online
            WHERE diary_no = ?
            AND diary_yr = ?
            """;

        List<ApplicationOwnershipInfo> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    ApplicationOwnershipInfo obj = new ApplicationOwnershipInfo();
                    obj.setStatus(rs.getString("status"));
                    obj.setApplappby(rs.getString("applappby"));
                    return obj;
                },
                diaryNo,
                diaryYr
        );

        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Maps a named ordering mode to a safe, fixed SQL ORDER BY fragment.
     * No user-supplied SQL is ever concatenated into the query.
     */
    private String resolveOrderBy(ApplicationOrderMode mode) {
        ApplicationOrderMode resolved =
                mode == null ? ApplicationOrderMode.LATEST_ACTION : mode;
        return switch (resolved) {
            case LATEST_ACTION -> """
                    (
                        SELECT MAX(e.entry_date)
                        FROM judl.efiling_log e
                        WHERE e.diaryno = diary_no
                          AND e.diary_yr = diary_yr
                          AND e.source = 'e-Inspection'
                    ) DESC NULLS LAST,
                    diary_yr DESC,
                    diary_no DESC""";
        };
    }
}
