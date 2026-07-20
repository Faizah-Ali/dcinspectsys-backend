package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.UploadHistoryResponse;

import java.util.List;

public interface UploadHistoryDAO {

    List<UploadHistoryResponse> getUploadHistory(int diaryNo, int diaryYr);

}
