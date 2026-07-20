package com.dhc.inspection_system.service;

import com.dhc.inspection_system.dto.UploadHistoryResponse;

import java.util.List;

public interface UploadHistoryService {

    List<UploadHistoryResponse> getUploadHistory(int diaryNo, int diaryYr);

}
