package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.DownloadDAO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadServiceImplTest {

    private static final String UNIQUE_ID = "ipcgjvojbbybjdigtdcqgUXIAYTY6042167";
    private static final String FILE_NAME = "40427-2026.pdf";

    @Mock
    private DownloadDAO downloadDAO;

    @InjectMocks
    private DownloadServiceImpl downloadService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(downloadService, "uploadDirectoryPath", tempDir.toString());
    }

    @Test
    void historyDownloadSucceedsForHistoricalActiveFile() throws Exception {
        Files.writeString(tempDir.resolve(FILE_NAME), "%PDF-1.4");
        when(downloadDAO.findHistoryFileNameByUniqueId(UNIQUE_ID))
                .thenReturn(Optional.of(FILE_NAME));

        Resource resource = downloadService.getHistoryDownloadResource(UNIQUE_ID);

        assertEquals(FILE_NAME, resource.getFilename());
        assertTrue(resource.exists());
        verify(downloadDAO, never()).findFileNameByUniqueId(UNIQUE_ID);
    }

    @Test
    void historyDownloadSucceedsForCurrentActiveFile() throws Exception {
        Files.writeString(tempDir.resolve(FILE_NAME), "%PDF-1.4");
        when(downloadDAO.findHistoryFileNameByUniqueId(UNIQUE_ID))
                .thenReturn(Optional.of(FILE_NAME));

        Resource resource = downloadService.getHistoryDownloadResource(UNIQUE_ID);

        assertEquals(FILE_NAME, resource.getFilename());
    }

    @Test
    void historyDownloadSucceedsForSoftDeletedFileWhenPhysicalPdfExists() throws Exception {
        Files.writeString(tempDir.resolve(FILE_NAME), "%PDF-1.4");
        when(downloadDAO.findHistoryFileNameByUniqueId(UNIQUE_ID))
                .thenReturn(Optional.of(FILE_NAME));

        Resource resource = downloadService.getHistoryDownloadResource(UNIQUE_ID);

        assertEquals(FILE_NAME, resource.getFilename());
        assertTrue(resource.exists());
    }

    @Test
    void historyDownloadReturnsNotFoundForUnknownUniqueId() {
        when(downloadDAO.findHistoryFileNameByUniqueId(UNIQUE_ID))
                .thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> downloadService.getHistoryDownloadResource(UNIQUE_ID)
        );

        assertEquals("File not found.", error.getMessage());
    }

    @Test
    void historyDownloadReturnsMissingDocumentWhenPhysicalFileAbsent() {
        when(downloadDAO.findHistoryFileNameByUniqueId(UNIQUE_ID))
                .thenReturn(Optional.of(FILE_NAME));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> downloadService.getHistoryDownloadResource(UNIQUE_ID)
        );

        assertEquals("Document does not exist on server.", error.getMessage());
    }

    @Test
    void timedDownloadStillUsesSixDayLookup() throws Exception {
        Files.writeString(tempDir.resolve(FILE_NAME), "%PDF-1.4");
        when(downloadDAO.findFileNameByUniqueId(UNIQUE_ID))
                .thenReturn(Optional.of(FILE_NAME));

        Resource resource = downloadService.getDownloadResource(UNIQUE_ID);

        assertEquals(FILE_NAME, resource.getFilename());
        verify(downloadDAO).findFileNameByUniqueId(UNIQUE_ID);
        verify(downloadDAO, never()).findHistoryFileNameByUniqueId(UNIQUE_ID);
    }
}
