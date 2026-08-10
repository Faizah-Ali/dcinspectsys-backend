package com.dhc.inspection_system.dto;

public class DeleteInspectionFileRequest {

    private String uniqueId;
    private int diaryNo;
    private int diaryYr;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getDiaryNo() {
        return diaryNo;
    }

    public void setDiaryNo(int diaryNo) {
        this.diaryNo = diaryNo;
    }

    public int getDiaryYr() {
        return diaryYr;
    }

    public void setDiaryYr(int diaryYr) {
        this.diaryYr = diaryYr;
    }
}
