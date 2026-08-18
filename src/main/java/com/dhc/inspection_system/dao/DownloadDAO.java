package com.dhc.inspection_system.dao;

import java.util.Optional;

public interface DownloadDAO {

    Optional<String> findFileNameByUniqueId(String uniqueId);

    /**
     * Staff Upload History download: no 6-day expiry; includes soft-deleted files.
     */
    Optional<String> findHistoryFileNameByUniqueId(String uniqueId);

}
