package com.dhc.inspection_system.service;

import java.sql.Timestamp;

public interface SmsQueueService {

    void queueSmsForCompletedApplication(int diaryNo, int diaryYr, Timestamp cycleCutoff);
}
