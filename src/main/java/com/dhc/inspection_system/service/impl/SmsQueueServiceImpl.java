package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.SmsQueueDAO;
import com.dhc.inspection_system.dto.OnlineInspectionSmsRow;
import com.dhc.inspection_system.service.SmsQueueService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
public class SmsQueueServiceImpl implements SmsQueueService {

    private static final String SMS_PURPOSE = "DATA_SHARING";
    private static final String SMS_SENT_BY = "99999999";
    private static final String SMS_TEMPLATE_ID = "1107166842128822368";

    @Autowired
    private SmsQueueDAO smsQueueDAO;

    // Isolated TX so queue failure cannot mark Complete rollback-only.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void queueSmsForCompletedApplication(
            int diaryNo,
            int diaryYr,
            Timestamp cycleCutoff
    ) {
        List<OnlineInspectionSmsRow> messages =
                smsQueueDAO.getOnlineInspectionSmsMessages(diaryNo, diaryYr, cycleCutoff);

        if (messages == null || messages.isEmpty()) {
            throw new RuntimeException(
                    "No inspection_user_online_message rows found for SMS queue"
            );
        }

        for (OnlineInspectionSmsRow row : messages) {
            String smsText = row.getSms() == null ? "" : row.getSms();
            String mobileNo = row.getMobile() == null ? "" : row.getMobile();

            int insertedRows = smsQueueDAO.insertSmsOperation(
                    smsText,
                    mobileNo,
                    SMS_PURPOSE,
                    SMS_SENT_BY,
                    SMS_TEMPLATE_ID
            );

            if (insertedRows <= 0) {
                throw new RuntimeException("Failed to insert sms_operation");
            }
        }
    }
}
