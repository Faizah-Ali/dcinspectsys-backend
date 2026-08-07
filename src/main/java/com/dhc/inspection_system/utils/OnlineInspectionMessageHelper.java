package com.dhc.inspection_system.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/** Builds legacy-equivalent inspection_user_online_message MESSAGE/SMS text. */
public final class OnlineInspectionMessageHelper {

    private OnlineInspectionMessageHelper() {
    }

    /**
     * Legacy getLimit(): current date + 5 days as dd/MM/yyyy.
     */
    public static String getLimit() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 5);
        return sdf.format(cal.getTime());
    }

    /**
     * Legacy mobile masking: ****** + last 4 digits (or 0000 if length &lt; 4).
     */
    public static String maskMobileLastFour(String mobile) {
        if (mobile == null || mobile.length() < 4) {
            return "0000";
        }
        return mobile.substring(mobile.length() - 4);
    }

    public static String buildDownloadUrl(String publicUrl, String uniqueId) {
        String base = publicUrl == null ? "" : publicUrl.trim();
        String id = uniqueId == null ? "" : uniqueId;
        if (base.endsWith("?")) {
            return base + "a=" + id;
        }
        if (base.contains("?")) {
            return base + "&a=" + id;
        }
        return base + "?a=" + id;
    }

    /**
     * Legacy Upload email HTML for inspection_user_online_message.MESSAGE.
     */
    public static String buildEmailMessage(
            int diaryNo,
            int diaryYr,
            String fileName,
            String uniqueId,
            String mobile,
            String downloadPublicUrl
    ) {
        String lastFour = maskMobileLastFour(mobile);
        String downloadHref = buildDownloadUrl(downloadPublicUrl, uniqueId);
        String safeFileName = fileName == null ? "" : fileName;

        return "<div>Dear Sir/Madam,<br><br>"
                + "<br><br>Please click on the link below to download file for e-inspection application file "
                + "applied vide Reference number "
                + diaryNo
                + "/"
                + diaryYr
                + " and File ID is "
                + safeFileName
                + "."
                + "<br>"
                + "This link will be active till "
                + getLimit()
                + ".<br>"
                + "<br>The file is password protected.<br>The password to open the file is sent on Registered Mobile number ******"
                + lastFour
                + ".<br>"
                + "Link to download file for e-Inspection is : <a href='"
                + downloadHref
                + "' >"
                + "Download File</a>.<br><br><br><br>"
                + " <span style='font-size: 25px;color:blue;'>Disclaimer:</span><span style='font-size: 12px;color:green;'>This is a "
                + "court document "
                + "and confidential in nature and these file(s) are for personal use of the applicant only and "
                + "neither be used for any other purpose nor be transmitted to a third party. The same shall not be shared in public "
                + "domain. If this mail is not meant for you, please do not download, and if downloaded, the same shall be deleted, failing which the  "
                + "legal consequences may follow.</span><br><br>"
                + "<span style='font-size: 25px;color:black;'>NOTE:</span>"
                + "The PDF Portfolio file can be opened for reading using Adobe Acrobat Reader which can be "
                + "downloaded from <a href='https://get.adobe.com/uk/reader/?promoid=TTGWL47M'>https://get.adobe.com/uk/reader/?promoid=TTGWL47M</a></div>";
    }

    /**
     * Legacy Upload SMS text for inspection_user_online_message.SMS.
     * File ID uses the uploaded filename (same value as email File ID).
     */
    public static String buildSmsMessage(
            String password,
            int diaryNo,
            int diaryYr,
            String fileId
    ) {
        String otp = password == null ? "" : password;
        String safeFileId = fileId == null ? "" : fileId;

        return otp
                + " is your OTP to open the PDF file received through email and e-inspection Reference number "
                + diaryNo
                + "/"
                + diaryYr
                + " and File ID "
                + safeFileId
                + ".Please do not share OTP with anyone.Delhi High Court.";
    }
}
