package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.EmailQueueDAO;
import com.dhc.inspection_system.dto.OnlineInspectionMessageRow;
import com.dhc.inspection_system.service.EmailQueueService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
public class EmailQueueServiceImpl implements EmailQueueService {

    private static final int LEGACY_CHUNK_SIZE = 3000;
    private static final String EMAIL_SUBJECT = "e-Inspection(Delhi High Court)";

    @Autowired
    private EmailQueueDAO emailQueueDAO;

    @Override
    public void queueEmailsForCompletedApplication(int diaryNo, int diaryYr) {
        List<OnlineInspectionMessageRow> messages =
                emailQueueDAO.getOnlineInspectionMessages(diaryNo, diaryYr);

        if (messages == null || messages.isEmpty()) {
            throw new RuntimeException(
                    "No inspection_user_online_message rows found for email queue"
            );
        }

        int year = Calendar.getInstance().get(Calendar.YEAR);

        for (OnlineInspectionMessageRow row : messages) {
            String emailId = row.getEmail() == null ? "" : row.getEmail();
            String contents = row.getMessage() == null ? "" : row.getMessage();

            String[] chunks = split(contents, LEGACY_CHUNK_SIZE);

            int orderId = emailQueueDAO.nextOrderId();
            String generatedId = orderId + "_" + year + "_" + emailId;

            int operationRows = emailQueueDAO.insertEmailOperation(
                    emailId,
                    generatedId,
                    EMAIL_SUBJECT
            );
            if (operationRows <= 0) {
                throw new RuntimeException("Failed to insert email_operation");
            }

            for (String chunk : chunks) {
                int contentRows = emailQueueDAO.insertEmailMessageContent(chunk, generatedId);
                if (contentRows <= 0) {
                    throw new RuntimeException("Failed to insert email_message_counter");
                }
            }
        }
    }

    @Override
    public void queueRejectEmail(
            int diaryNo,
            int diaryYr,
            String email,
            String remarks
    ) {
        String emailId = email == null ? "" : email;
        String safeRemarks = remarks == null ? "" : remarks;
        String message = "<div>Dear Sir/Madam,<br><br>&nbsp;&nbsp;&nbsp;&nbsp;</br></br>e-Inspection application filed vide Reference NO. "
                + diaryNo
                + "/"
                + diaryYr
                + " has been rejected due to following reason:</div><br><br>&nbsp;&nbsp;&nbsp;&nbsp;"
                + safeRemarks;

        int year = Calendar.getInstance().get(Calendar.YEAR);
        String[] chunks = split(message, LEGACY_CHUNK_SIZE);

        int orderId = emailQueueDAO.nextOrderId();
        String generatedId = orderId + "_" + year + "_" + emailId;

        int operationRows = emailQueueDAO.insertEmailOperation(
                emailId,
                generatedId,
                EMAIL_SUBJECT
        );
        if (operationRows <= 0) {
            throw new RuntimeException("Failed to insert email_operation");
        }

        for (String chunk : chunks) {
            int contentRows = emailQueueDAO.insertEmailMessageContent(chunk, generatedId);
            if (contentRows <= 0) {
                throw new RuntimeException("Failed to insert email_message_counter");
            }
        }
    }

    /**
     * Legacy MyUtil.split(src, len) behavior.
     */
    static String[] split(String src, int len) {
        String source = src == null ? "" : src;
        int size = (int) Math.ceil((double) source.length() / (double) len);
        if (size == 0) {
            return new String[0];
        }

        String[] result = new String[size];
        for (int i = 0; i < result.length; i++) {
            result[i] = source.substring(i * len, Math.min(source.length(), (i + 1) * len));
        }
        return result;
    }
}
