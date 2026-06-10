package com.dhc.inspection_system.dto;

public class ApplicationResponse {

    private String username;
    private String casetype;
    private int regNo;
    private int regYr;
    private int diaryNo;
    private int diaryYr;
    private String remarks;
    private String ecourtFeeId;
    private String caseTitle;
    private String appliedDate;
    private String status;
    private String caseStatus;
    private String ecourtMessage;
    private String courtFeeAmount;
    private String courtFeeReason;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCasetype() {
        return casetype;
    }

    public void setCasetype(String casetype) {
        this.casetype = casetype;
    }

    public int getRegNo() {
        return regNo;
    }

    public void setRegNo(int regNo) {
        this.regNo = regNo;
    }

    public int getRegYr() {
        return regYr;
    }

    public void setRegYr(int regYr) {
        this.regYr = regYr;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getEcourtFeeId() {
        return ecourtFeeId;
    }

    public void setEcourtFeeId(String ecourtFeeId) {
        this.ecourtFeeId = ecourtFeeId;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus;
    }

    public String getEcourtMessage() {
        return ecourtMessage;
    }

    public void setEcourtMessage(String ecourtMessage) {
        this.ecourtMessage = ecourtMessage;
    }

    public String getCourtFeeAmount() {
        return courtFeeAmount;
    }

    public void setCourtFeeAmount(String courtFeeAmount) {
        this.courtFeeAmount = courtFeeAmount;
    }

    public String getCourtFeeReason() {
        return courtFeeReason;
    }

    public void setCourtFeeReason(String courtFeeReason) {
        this.courtFeeReason = courtFeeReason;
    }
}