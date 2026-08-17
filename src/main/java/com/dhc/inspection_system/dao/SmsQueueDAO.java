package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.OnlineInspectionSmsRow;

import java.sql.Timestamp;
import java.util.List;

public interface SmsQueueDAO {

    List<OnlineInspectionSmsRow> getOnlineInspectionSmsMessages(
            int diaryNo,
            int diaryYr,
            Timestamp cycleCutoff
    );

    int insertSmsOperation(
            String message,
            String mobileNo,
            String purpose,
            String smsSentBy,
            String templateId
    );
}
