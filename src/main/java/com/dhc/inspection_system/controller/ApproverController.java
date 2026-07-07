package com.dhc.inspection_system.controller;

import com.dhc.inspection_system.dto.ApproverResponse;
import com.dhc.inspection_system.service.ApproverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApproverController {

    @Autowired
    private ApproverService approverService;

    @GetMapping("/approvers-list")
    public List<ApproverResponse> getApproversList() {
        return approverService.getApproversList();
    }
}
