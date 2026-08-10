package com.dhc.inspection_system.service;

import com.dhc.inspection_system.dto.DeleteInspectionFileRequest;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    int uploadInspectionFiles(MultipartFile[] files, String diaryNo, String diaryYr, String authorization);

    int deleteInspectionFile(String authorization, DeleteInspectionFileRequest request);

}
