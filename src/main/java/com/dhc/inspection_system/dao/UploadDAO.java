package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.UploadApplicantDetails;

import org.springframework.web.multipart.MultipartFile;

public interface UploadDAO {

    void saveInspectionFile(MultipartFile file, String fileName);

    UploadApplicantDetails getApplicantDetails(int diaryNo, int diaryYr);

    int insertDataShareReceiverDetails(
            String name,
            String mobileNo,
            String emailId,
            String entryBy,
            String fileName,
            String loginId,
            String password,
            int diaryNo,
            int diaryYr,
            String uniqueId
    );

    int saveOnlineInspectionMessage(
            int diaryNo,
            int diaryYr,
            String message,
            String email,
            String mobile,
            String sms
    );

}
