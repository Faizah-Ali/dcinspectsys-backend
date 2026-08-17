package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.OnlineInspectionMessageRow;

import java.sql.Timestamp;
import java.util.List;

public interface EmailQueueDAO {

    List<OnlineInspectionMessageRow> getOnlineInspectionMessages(
            int diaryNo,
            int diaryYr,
            Timestamp cycleCutoff
    );

    int nextOrderId();

    int insertEmailOperation(String emailId, String generatedId, String subject);

    int insertEmailMessageContent(String messageChunk, String generatedId);
}
