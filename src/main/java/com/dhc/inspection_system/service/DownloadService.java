package com.dhc.inspection_system.service;

import org.springframework.core.io.Resource;

public interface DownloadService {

    Resource getDownloadResource(String uniqueId);

}
