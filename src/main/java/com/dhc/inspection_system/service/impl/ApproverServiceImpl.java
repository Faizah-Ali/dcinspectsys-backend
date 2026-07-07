package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.ApproverDao;
import com.dhc.inspection_system.dto.ApproverResponse;
import com.dhc.inspection_system.service.ApproverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApproverServiceImpl implements ApproverService {

    @Autowired
    private ApproverDao approverDao;

    @Override
    public List<ApproverResponse> getApproversList() {
        return approverDao.getApproversList();
    }
}
