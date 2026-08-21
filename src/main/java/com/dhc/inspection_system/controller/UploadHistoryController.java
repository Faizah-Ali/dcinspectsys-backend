package com.dhc.inspection_system.controller;

import com.dhc.inspection_system.dto.UploadHistoryWrapperResponse;
import com.dhc.inspection_system.dto.UserCommentResponse;
import com.dhc.inspection_system.service.UploadHistoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UploadHistoryController {

    @Autowired
    private UploadHistoryService uploadHistoryService;

    @GetMapping("/upload-history")
    public UploadHistoryWrapperResponse getUploadHistory(
            @RequestParam("diaryNo") int diaryNo,
            @RequestParam("diaryYr") int diaryYr
    ) {
        return uploadHistoryService.getUploadHistory(diaryNo, diaryYr);
    }

    @GetMapping("/inspection-comments")
    public List<UserCommentResponse> getInspectionComments(
            @RequestParam("diaryNo") int diaryNo,
            @RequestParam("diaryYr") int diaryYr
    ) {
        return uploadHistoryService.getInspectionComments(diaryNo, diaryYr);
    }

}
