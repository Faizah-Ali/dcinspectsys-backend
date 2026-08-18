package com.dhc.inspection_system.controller;

import com.dhc.inspection_system.service.DownloadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RestController
@RequestMapping("/api")
public class DownloadController {

    @Autowired
    private DownloadService downloadService;

    @GetMapping("/download-inspection-file")
    public ResponseEntity<?> downloadInspectionFile(
            @RequestParam("uniqueId") String uniqueId
    ) {
        return downloadPdf(uniqueId, downloadService::getDownloadResource);
    }

    /**
     * Staff Upload History preview/download — no 6-day expiry; includes soft-deleted files.
     */
    @GetMapping("/download-history-file")
    public ResponseEntity<?> downloadHistoryFile(
            @RequestParam("uniqueId") String uniqueId
    ) {
        return downloadPdf(uniqueId, downloadService::getHistoryDownloadResource);
    }

    private ResponseEntity<?> downloadPdf(
            String uniqueId,
            Function<String, Resource> resourceLoader
    ) {
        try {
            Resource resource = resourceLoader.apply(uniqueId);
            String fileName = resource.getFilename();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Unable to download document.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
