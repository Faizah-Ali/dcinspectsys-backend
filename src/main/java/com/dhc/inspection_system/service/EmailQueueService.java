package com.dhc.inspection_system.service;

public interface EmailQueueService {

    void queueEmailsForCompletedApplication(int diaryNo, int diaryYr);

    void queueRejectEmail(
            int diaryNo,
            int diaryYr,
            String email,
            String remarks
    );
}
