package com.dhc.inspection_system.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    int uploadInspectionFiles(MultipartFile[] files, String diaryNo, String diaryYr, String authorization);

}
