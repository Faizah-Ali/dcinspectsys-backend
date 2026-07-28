package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.InspectionLogResponse;
import com.dhc.inspection_system.dto.UploadHistoryResponse;
import com.dhc.inspection_system.dto.UserCommentResponse;

import java.util.List;

public interface UploadHistoryDAO {

    List<UploadHistoryResponse> getUploadHistory(int diaryNo, int diaryYr);

    List<InspectionLogResponse> getInspectionLogs(Integer diaryNo, Integer diaryYr);

    List<UserCommentResponse> getUserComments(Integer diaryNo, Integer diaryYr);

    int saveOfficeComment(Integer diaryNo, Integer diaryYr, String content, String author);

}
