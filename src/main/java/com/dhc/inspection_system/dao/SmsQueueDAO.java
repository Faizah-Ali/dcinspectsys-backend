package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.OnlineInspectionSmsRow;

import java.util.List;

public interface SmsQueueDAO {

    List<OnlineInspectionSmsRow> getOnlineInspectionSmsMessages(int diaryNo, int diaryYr);

    int insertSmsOperation(
            String message,
            String mobileNo,
            String purpose,
            String smsSentBy,
            String templateId
    );
}
