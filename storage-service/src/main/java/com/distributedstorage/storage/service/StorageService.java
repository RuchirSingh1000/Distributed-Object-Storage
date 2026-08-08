package com.distributedstorage.storage.service;

import com.distributedstorage.common.model.FileChunk;
import com.distributedstorage.storage.config.MinIOProperties;
import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Service for handling file storage operations using MinIO
 */
@Service
@RequiredArgsConstructor
public class StorageService {
    private final MinioClient minioClient;
    private final MinIOProperties minioProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.file-events}")
    private String kafkaTopicFileEvents;

    /**
     * Upload a file to MinIO and send event to Kafka
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String fileId = UUID.randomUUID().toString();
        String bucketName = minioProperties.getBucketName();

        try {
            // Ensure bucket exists
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
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
            kafkaTemplate.send(kafkaTopicFileEvents, event);

            return fileId;
        } catch (MinioException e) {
            throw new IOException("MinIO operation failed: " + e.getMessage(), e);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IOException("Security error during file upload: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Unexpected error during file upload: " + e.getMessage(), e);
        }
    }

    /**
     * Download a file from MinIO
     */
    public InputStream downloadFile(String fileId) {
        try {
            String bucketName = minioProperties.getBucketName();
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileId)
                    .build());
        } catch (MinioException e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file from MinIO
     */
    public void deleteFile(String fileId) {
        try {
            String bucketName = minioProperties.getBucketName();
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileId)
                    .build());
        } catch (MinioException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }
}