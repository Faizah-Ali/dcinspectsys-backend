package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.UploadHistoryDAO;
import com.dhc.inspection_system.dto.InspectionLogResponse;
import com.dhc.inspection_system.dto.UploadHistoryResponse;
import com.dhc.inspection_system.dto.UploadHistoryWrapperResponse;
import com.dhc.inspection_system.dto.UserCommentResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadHistoryServiceImplTest {

    private static final int DIARY_NO = 148426;
    private static final int DIARY_YR = 2026;

    @Mock
    private UploadHistoryDAO uploadHistoryDAO;

    @InjectMocks
    private UploadHistoryServiceImpl uploadHistoryService;

    @Test
    void getInspectionCommentsReturnsCommentsFromDao() {
        UserCommentResponse comment = new UserCommentResponse();
        comment.setContent("Dealing note");
        comment.setAuthor("Officer Name");
        comment.setCommentDate("2026-08-21 10:00:00");
        List<UserCommentResponse> expected = List.of(comment);

        when(uploadHistoryDAO.getUserComments(DIARY_NO, DIARY_YR)).thenReturn(expected);

        List<UserCommentResponse> actual =
                uploadHistoryService.getInspectionComments(DIARY_NO, DIARY_YR);

        assertSame(expected, actual);
        verify(uploadHistoryDAO).getUserComments(DIARY_NO, DIARY_YR);
        verify(uploadHistoryDAO, never()).getUploadHistory(DIARY_NO, DIARY_YR);
        verify(uploadHistoryDAO, never()).getInspectionLogs(DIARY_NO, DIARY_YR);
    }

    @Test
    void getInspectionCommentsReturnsEmptyListWhenNoComments() {
        when(uploadHistoryDAO.getUserComments(DIARY_NO, DIARY_YR))
                .thenReturn(Collections.emptyList());

        List<UserCommentResponse> actual =
                uploadHistoryService.getInspectionComments(DIARY_NO, DIARY_YR);

        assertTrue(actual.isEmpty());
        verify(uploadHistoryDAO).getUserComments(DIARY_NO, DIARY_YR);
        verify(uploadHistoryDAO, never()).getUploadHistory(DIARY_NO, DIARY_YR);
        verify(uploadHistoryDAO, never()).getInspectionLogs(DIARY_NO, DIARY_YR);
    }

    @Test
    void getUploadHistoryStillReturnsFilesLogsAndComments() {
        List<UploadHistoryResponse> files = List.of(new UploadHistoryResponse());
        List<InspectionLogResponse> logs = List.of(new InspectionLogResponse());
        List<UserCommentResponse> comments = List.of(new UserCommentResponse());

        when(uploadHistoryDAO.getUploadHistory(DIARY_NO, DIARY_YR)).thenReturn(files);
        when(uploadHistoryDAO.getInspectionLogs(DIARY_NO, DIARY_YR)).thenReturn(logs);
        when(uploadHistoryDAO.getUserComments(DIARY_NO, DIARY_YR)).thenReturn(comments);

        UploadHistoryWrapperResponse response =
                uploadHistoryService.getUploadHistory(DIARY_NO, DIARY_YR);

        assertEquals(files, response.getUploadedFiles());
        assertEquals(logs, response.getInspectionLogs());
        assertEquals(comments, response.getUserComments());
    }
}
