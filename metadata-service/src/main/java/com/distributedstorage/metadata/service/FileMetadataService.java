package com.distributedstorage.metadata.service;

import com.distributedstorage.metadata.model.FileMetadata;
import com.distributedstorage.metadata.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing file metadata
 */
@Service
@RequiredArgsConstructor
public class FileMetadataService {
    private final FileMetadataRepository fileMetadataRepository;

    public FileMetadata createFileMetadata(String fileName, Long fileSize, String contentType, String checksum) {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileId(UUID.randomUUID().toString());
        metadata.setFileName(fileName);
        metadata.setFileSize(fileSize);
        metadata.setContentType(contentType);
        metadata.setOverallChecksum(checksum);
        metadata.setUploadTimestamp(Instant.now());
        metadata.setStatus("UPLOADED");
        return fileMetadataRepository.save(metadata);
    }

    public FileMetadata getFileMetadata(String fileId) {
        return fileMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new RuntimeException("File metadata not found for ID: " + fileId));
    }

    public List<FileMetadata> getAllFileMetadata() {
        return fileMetadataRepository.findAll();
    }

    public void deleteFileMetadata(String fileId) {
        fileMetadataRepository.deleteById(
                fileMetadataRepository.findByFileId(fileId)
                        .orElseThrow(() -> new RuntimeException("File metadata not found for ID: " + fileId))
                        .getId());
    }

    public FileMetadata updateFileStatus(String fileId, String status) {
        FileMetadata metadata = getFileMetadata(fileId);
        metadata.setStatus(status);
        return fileMetadataRepository.save(metadata);
    }
}