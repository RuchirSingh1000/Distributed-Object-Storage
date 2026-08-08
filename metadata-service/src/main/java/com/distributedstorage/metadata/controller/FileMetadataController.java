package com.distributedstorage.metadata.controller;

import com.distributedstorage.metadata.model.FileMetadata;
import com.distributedstorage.metadata.service.FileMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for file metadata operations
 */
@RestController
@RequestMapping("/api/v1/metadata")
@RequiredArgsConstructor
public class FileMetadataController {
    private final FileMetadataService fileMetadataService;

    @PostMapping
    public ResponseEntity<FileMetadata> createFileMetadata(
            @RequestParam String fileName,
            @RequestParam Long fileSize,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String checksum) {
        FileMetadata metadata = fileMetadataService.createFileMetadata(
                fileName, fileSize, contentType, checksum);
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileMetadata> getFileMetadata(@PathVariable String fileId) {
        FileMetadata metadata = fileMetadataService.getFileMetadata(fileId);
        return ResponseEntity.ok(metadata);
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> getAllFileMetadata() {
        List<FileMetadata> metadataList = fileMetadataService.getAllFileMetadata();
        return ResponseEntity.ok(metadataList);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFileMetadata(@PathVariable String fileId) {
        fileMetadataService.deleteFileMetadata(fileId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{fileId}/status")
    public ResponseEntity<FileMetadata> updateFileStatus(
            @PathVariable String fileId,
            @RequestParam String status) {
        FileMetadata metadata = fileMetadataService.updateFileStatus(fileId, status);
        return ResponseEntity.ok(metadata);
    }
}