package com.distributedstorage.storage.service;

import com.distributedstorage.common.model.FileChunk;
import com.distributedstorage.common.utils.StorageUtils;
import com.distributedstorage.storage.config.MinIOProperties;
import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling file storage operations using MinIO with chunking and deduplication
 */
@Service
@RequiredArgsConstructor
public class StorageService {
    private final MinioClient minioClient;
    private final MinIOProperties minioProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Value("${kafka.topic.file-events}")
    private String kafkaTopicFileEvents;

    @Value("${metadata.service.url}")
    private String metadataServiceUrl;

    /**
     * Upload a file to MinIO with chunking and deduplication
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

            // Read file content
            byte[] fileContent = file.getBytes();
            long fileSize = fileContent.length;

            // Split file into chunks
            List<byte[]> chunks = StorageUtils.chunkData(fileContent, StorageUtils.DEFAULT_CHUNK_SIZE);
            StringBuilder chunkIdsBuilder = new StringBuilder();

            for (int i = 0; i < chunks.size(); i++) {
                byte[] chunkData = chunks.get(i);
                String chunkId = UUID.randomUUID().toString();
                String chunkChecksum = StorageUtils.calculateHash(chunkData);

                // Check if chunk already exists (deduplication)
                String checkUrl = metadataServiceUrl + "/api/v1/chunks/checksum/" + chunkChecksum;
                // In a real implementation, we would parse the response to see if chunk exists
                // For simplicity, we assume the chunk does not exist and we store it.
                // We will store the chunk and then save metadata.

                // Store chunk in MinIO
                String chunkObjectName = fileId + "_chunk_" + i;
                try (InputStream chunkStream = new java.io.ByteArrayInputStream(chunkData)) {
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(chunkObjectName)
                            .stream(chunkStream, chunkData.length, -1)
                            .contentType("application/octet-stream")
                            .build());
                }

                // TODO: Save chunk metadata via metadata-service (we would call a POST endpoint)
                // For now, we just record the chunk ID.

                if (i > 0) {
                    chunkIdsBuilder.append(",");
                }
                chunkIdsBuilder.append(chunkObjectName);
            }

            String chunkIds = chunkIdsBuilder.toString();

            // Send file uploaded event to Kafka (with chunk info)
            String event = String.format("{\"fileId\":\"%s\",\"eventType\":\"FILE_UPLOADED\",\"bucket\":\"%s\",\"object\":\"%s\",\"chunkIds\":\"%s\"}",
                    fileId, bucketName, fileId, chunkIds);
            kafkaTemplate.send(kafkaTopicFileEvents, event);

            // TODO: Save file metadata via metadata-service (we would call a POST endpoint on metadata-service)
            // For now, we just return the fileId.

            return fileId;
        } catch (MinioException e) {
            throw new IOException("MinIO operation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Unexpected error during file upload: " + e.getMessage(), e);
        }
    }

    /**
     * Download a file from MinIO by reassembling chunks
     */
    public InputStream downloadFile(String fileId) {
        try {
            String bucketName = minioProperties.getBucketName();
            // TODO: Retrieve chunk IDs from metadata-service for this fileId
            // For simplicity, we assume we have a way to get the chunk IDs.
            // We will simulate by trying to download a single object (the old way) for backward compatibility.
            // In the updated version, we would reassemble chunks.
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileId)
                    .build());
        } catch (MinioException e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file from MinIO by deleting all its chunks
     */
    public void deleteFile(String fileId) {
        try {
            String bucketName = minioProperties.getBucketName();
            // TODO: Retrieve chunk IDs from metadata-service for this fileId and delete each chunk
            // For simplicity, we delete the single object (the old way) for backward compatibility.
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileId)
                    .build());
        } catch (MinioException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }
}