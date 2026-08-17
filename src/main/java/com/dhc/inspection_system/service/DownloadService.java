package com.dhc.inspection_system.service;

import org.springframework.core.io.Resource;

public interface DownloadService {

    Resource getDownloadResource(String uniqueId);

    /**
     * Staff Upload History download/preview — no 6-day expiry.
     */
    Resource getHistoryDownloadResource(String uniqueId);

}
