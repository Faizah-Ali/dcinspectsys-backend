package com.dhc.inspection_system.dto;

import java.time.LocalDateTime;

public class UploadHistoryResponse {

    private String uniqueId;
    private String emailId;
    private String fileName;
    private Long diaryNo;
    private Integer diaryYr;
    private String mobileNo;
    private LocalDateTime entryDate;
    private String entryBy;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getDiaryNo() {
        return diaryNo;
    }

    public void setDiaryNo(Long diaryNo) {
        this.diaryNo = diaryNo;
    }

    public Integer getDiaryYr() {
        return diaryYr;
    }

    public void setDiaryYr(Integer diaryYr) {
        this.diaryYr = diaryYr;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public LocalDateTime getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDateTime entryDate) {
        this.entryDate = entryDate;
    }

    public String getEntryBy() {
        return entryBy;
    }

    public void setEntryBy(String entryBy) {
        this.entryBy = entryBy;
    }
}
