package com.distributedstorage.storage.controller;

import com.distributedstorage.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * REST controller for storage operations
 */
@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileId = storageService.uploadFile(file);
            return ResponseEntity.ok(fileId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<InputStream> downloadFile(@PathVariable String fileId) {
        InputStream inputStream = storageService.downloadFile(fileId);
        return ResponseEntity.ok()
                .body(inputStream);
    }

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
        storageService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
}