package com.dhc.inspection_system.service;

public interface SmsQueueService {

    void queueSmsForCompletedApplication(int diaryNo, int diaryYr);
}
