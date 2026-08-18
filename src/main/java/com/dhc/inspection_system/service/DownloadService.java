package com.dhc.inspection_system.service;

import org.springframework.core.io.Resource;

public interface DownloadService {

    Resource getDownloadResource(String uniqueId);

    /**
     * Staff Upload History download/preview — no 6-day expiry; includes soft-deleted files.
     */
    Resource getHistoryDownloadResource(String uniqueId);

}
