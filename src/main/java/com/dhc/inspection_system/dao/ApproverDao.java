package com.dhc.inspection_system.dao;

import com.dhc.inspection_system.dto.ApproverResponse;

import java.util.List;

public interface ApproverDao {

    List<ApproverResponse> getApproversList();
}
