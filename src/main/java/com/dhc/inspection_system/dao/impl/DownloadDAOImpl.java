package com.dhc.inspection_system.dao.impl;

import com.dhc.inspection_system.dao.DownloadDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DownloadDAOImpl implements DownloadDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Optional<String> findFileNameByUniqueId(String uniqueId) {
        String sql = """
            SELECT file_name
            FROM judl.data_share_receiver_details
            WHERE uniqueid = ?
            AND EXTRACT(DAY FROM CURRENT_TIMESTAMP - entry_date) < 6
            """;

        List<String> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("file_name"),
                uniqueId
        );

        if (results.isEmpty() || results.get(0) == null || results.get(0).isBlank()) {
            return Optional.empty();
        }

        return Optional.of(results.get(0));
    }

}
