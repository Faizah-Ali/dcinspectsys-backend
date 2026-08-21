package com.dhc.inspection_system.service;

import com.dhc.inspection_system.dto.UploadHistoryWrapperResponse;
import com.dhc.inspection_system.dto.UserCommentResponse;

import java.util.List;

public interface UploadHistoryService {

    UploadHistoryWrapperResponse getUploadHistory(int diaryNo, int diaryYr);

    List<UserCommentResponse> getInspectionComments(int diaryNo, int diaryYr);

}
