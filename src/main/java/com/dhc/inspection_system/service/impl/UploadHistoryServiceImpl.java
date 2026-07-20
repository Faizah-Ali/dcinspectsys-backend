package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.UploadHistoryDAO;
import com.dhc.inspection_system.dto.UploadHistoryResponse;
import com.dhc.inspection_system.service.UploadHistoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UploadHistoryServiceImpl implements UploadHistoryService {

    @Autowired
    private UploadHistoryDAO uploadHistoryDAO;

    @Override
    public List<UploadHistoryResponse> getUploadHistory(int diaryNo, int diaryYr) {
        return uploadHistoryDAO.getUploadHistory(diaryNo, diaryYr);
    }

}
