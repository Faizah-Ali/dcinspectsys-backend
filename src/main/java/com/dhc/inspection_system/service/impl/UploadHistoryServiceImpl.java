package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.UploadHistoryDAO;
import com.dhc.inspection_system.dto.UploadHistoryWrapperResponse;
import com.dhc.inspection_system.service.UploadHistoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UploadHistoryServiceImpl implements UploadHistoryService {

    @Autowired
    private UploadHistoryDAO uploadHistoryDAO;

    @Override
    public UploadHistoryWrapperResponse getUploadHistory(int diaryNo, int diaryYr) {
        UploadHistoryWrapperResponse response = new UploadHistoryWrapperResponse();
        response.setUploadedFiles(uploadHistoryDAO.getUploadHistory(diaryNo, diaryYr));
        response.setInspectionLogs(uploadHistoryDAO.getInspectionLogs(diaryNo, diaryYr));
        response.setUserComments(uploadHistoryDAO.getUserComments(diaryNo, diaryYr));
        return response;
    }

}
