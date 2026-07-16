package com.dhc.inspection_system.dto;

public class SendForApprovalRequest {

    private Integer diaryNo;
    private Integer diaryYr;
    private String approverId;
    private String approverName;
    private String remarks;

    public Integer getDiaryNo() {
        return diaryNo;
    }

    public void setDiaryNo(Integer diaryNo) {
        this.diaryNo = diaryNo;
    }

    public Integer getDiaryYr() {
        return diaryYr;
    }

    public void setDiaryYr(Integer diaryYr) {
        this.diaryYr = diaryYr;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
