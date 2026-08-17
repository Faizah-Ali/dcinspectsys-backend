package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dao.DownloadDAO;
import com.dhc.inspection_system.service.DownloadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

@Service
public class DownloadServiceImpl implements DownloadService {

    @Value("${inspection.upload.path}")
    private String uploadDirectoryPath;

    @Autowired
    private DownloadDAO downloadDAO;

    @Override
    public Resource getDownloadResource(String uniqueId) {
        return resolveResource(uniqueId, downloadDAO::findFileNameByUniqueId);
    }

    @Override
    public Resource getHistoryDownloadResource(String uniqueId) {
        return resolveResource(uniqueId, downloadDAO::findHistoryFileNameByUniqueId);
    }

    private Resource resolveResource(
            String uniqueId,
            Function<String, Optional<String>> fileNameLookup
    ) {
        if (uniqueId == null || uniqueId.isBlank()) {
            throw new IllegalArgumentException("File not found.");
        }

        Optional<String> fileNameOpt = fileNameLookup.apply(uniqueId.trim());
        if (fileNameOpt.isEmpty()) {
            throw new IllegalArgumentException("File not found.");
        }

        String fileName = fileNameOpt.get();
        Path filePath = Paths.get(uploadDirectoryPath, fileName);

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IllegalStateException("Document does not exist on server.");
        }

        return new FileSystemResource(filePath.toFile());
    }

}
