package com.dhc.inspection_system.service;

import com.dhc.inspection_system.dto.UploadHistoryWrapperResponse;

public interface UploadHistoryService {

    UploadHistoryWrapperResponse getUploadHistory(int diaryNo, int diaryYr);

}
