package com.dhc.inspection_system.dto;

import java.util.List;

public class UploadHistoryWrapperResponse {

    private List<UploadHistoryResponse> uploadedFiles;
    private List<InspectionLogResponse> inspectionLogs;
    private List<UserCommentResponse> userComments;

    public List<UploadHistoryResponse> getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(List<UploadHistoryResponse> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    public List<InspectionLogResponse> getInspectionLogs() {
        return inspectionLogs;
    }

    public void setInspectionLogs(List<InspectionLogResponse> inspectionLogs) {
        this.inspectionLogs = inspectionLogs;
    }

    public List<UserCommentResponse> getUserComments() {
        return userComments;
    }

    public void setUserComments(List<UserCommentResponse> userComments) {
        this.userComments = userComments;
    }
}
