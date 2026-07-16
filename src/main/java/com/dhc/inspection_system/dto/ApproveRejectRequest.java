package com.dhc.inspection_system.dto;

public class ApproveRejectRequest {

    private Integer diaryNo;
    private Integer diaryYr;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
