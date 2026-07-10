package com.dhc.inspection_system.dto;

public class AssignApplicationRequest {

    private Integer diaryNo;
    private Integer diaryYr;
    private String assigned;
    private String assignedname;
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

    public String getAssigned() {
        return assigned;
    }

    public void setAssigned(String assigned) {
        this.assigned = assigned;
    }

    public String getAssignedname() {
        return assignedname;
    }

    public void setAssignedname(String assignedname) {
        this.assignedname = assignedname;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
