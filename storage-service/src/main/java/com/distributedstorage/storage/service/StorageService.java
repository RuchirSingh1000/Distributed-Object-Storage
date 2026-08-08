package com.distributedstorage.storage.service;

import com.distributedstorage.common.model.FileChunk;
import com.distributedstorage.storage.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Service for handling file storage operations using MinIO
 */
@Service
@RequiredArgsConstructor
public class StorageService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Upload a file to MinIO and send event to Kafka
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String fileId = UUID.randomUUID().toString();
        String bucketName = minioProperties.getBucketName();

        // Ensure bucket exists
        boolean found = minioClient.bucketExists(bucketName);
        if (!found) {
            minioClient.makeBucket(bucketName);
        }

        // Upload file to MinIO
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileId)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        }

        // Send file uploaded event to Kafka
        String event = String.format("{\"fileId\":\"%s\",\"eventType\":\"FILE_UPLOADED\",\"bucket\":\"%s\",\"object\":\"%s\"}",
                fileId, bucketName, fileId);
        kafkaTemplate.send(minioProperties.getKafkaTopicFileEvents(), event);

        return fileId;
    }

    /**
     * Download a file from MinIO
     */
    public InputStream downloadFile(String fileId) {
        String bucketName = minioProperties.getBucketName();
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(fileId)
                .build());
    }

    /**
     * Delete a file from MinIO
     */
    public void deleteFile(String fileId) {
        String bucketName = minioProperties.getBucketName();
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fileId)
                .build());
    }
}