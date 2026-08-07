package com.dhc.inspection_system.utils;

public final class InspectionAuditLogHelper {

    private InspectionAuditLogHelper() {
    }

    public static String formatEmployeeIdentity(String empName, String empCode) {
        String name = empName == null ? "" : empName;
        String code = empCode == null ? "" : empCode;
        return name + "(" + code + ")";
    }

    public static String nullSafeRemarks(String remarks) {
        return remarks == null ? "" : remarks;
    }

    public static String buildAssignDescription(
            int diaryNo,
            int diaryYr,
            String assigneeName,
            String assigneeId,
            String remarks
    ) {
        return "e-Inspection application filed vide Reference No. "
                + diaryNo + "/" + diaryYr
                + " is Assigned to :"
                + formatEmployeeIdentity(assigneeName, assigneeId)
                + " for Processing. Comment is :"
                + nullSafeRemarks(remarks);
    }

    public static String buildForwardDescription(
            int diaryNo,
            int diaryYr,
            String approverName,
            String approverId,
            String remarks
    ) {
        return "e-Inspection application filed vide Reference No. "
                + diaryNo + "/" + diaryYr
                + " is forwarded to :"
                + formatEmployeeIdentity(approverName, approverId)
                + " for approval. Dealing comment is :"
                + nullSafeRemarks(remarks);
    }

    public static String buildApproverForwardDescription(
            int diaryNo,
            int diaryYr,
            String approverName,
            String approverId,
            String remarks
    ) {
        return "e-Inspection application filed vide Reference No. "
                + diaryNo + "/" + diaryYr
                + " is Assigned to :"
                + formatEmployeeIdentity(approverName, approverId)
                + " for further directions. Comment is :"
                + nullSafeRemarks(remarks);
    }

    public static String buildApproveDescription(
            int diaryNo,
            int diaryYr,
            String approverName,
            String approverId,
            String remarks
    ) {
        return "e-Inspection application filed vide Reference No. "
                + diaryNo + "/" + diaryYr
                + " is approved by :"
                + formatEmployeeIdentity(approverName, approverId)
                + ". Approver Comment is :"
                + nullSafeRemarks(remarks);
    }

    public static String buildRejectDescription(
            int diaryNo,
            int diaryYr,
            String approverName,
            String approverId,
            String remarks
    ) {
        return "e-Inspection application filed vide Reference No. "
                + diaryNo + "/" + diaryYr
                + " is rejected by :"
                + formatEmployeeIdentity(approverName, approverId)
                + ". Reason of rejection :"
                + nullSafeRemarks(remarks);
    }

    public static String buildRejectFailureDescription(
            int diaryNo,
            int diaryYr,
            String rejectorName,
            String username
    ) {
        return "e-Inspection application filed vide Reference No. "
                + diaryNo + "/" + diaryYr
                + " is not rejected by :"
                + formatEmployeeIdentity(rejectorName, username)
                + " due to some technical reason in software.";
    }

    public static String buildCompleteDescription(
            int diaryNo,
            int diaryYr,
            String username,
            String fullName
    ) {
        String user = username == null ? "" : username;
        String name = fullName == null ? "" : fullName;
        return "e-Inspection application filed vide Reference No. "
                + diaryNo + "/" + diaryYr
                + " is marked completd by :"
                + user + "(" + name + ")";
    }

    public static String buildCompleteFailureDescription(
            int diaryNo,
            int diaryYr,
            String username,
            String fullName
    ) {
        String user = username == null ? "" : username;
        String name = fullName == null ? "" : fullName;
        return "e-Inspection application filed vide Reference No. "
                + diaryNo + "/" + diaryYr
                + " is not marked completd by :"
                + user + "(" + name + ")"
                + " due to some technical glitch in software .";
    }
}
