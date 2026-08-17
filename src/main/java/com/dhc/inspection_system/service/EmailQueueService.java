package com.dhc.inspection_system.service;

import java.sql.Timestamp;

public interface EmailQueueService {

    void queueEmailsForCompletedApplication(int diaryNo, int diaryYr, Timestamp cycleCutoff);

    void queueRejectEmail(
            int diaryNo,
            int diaryYr,
            String email,
            String remarks
    );
}
