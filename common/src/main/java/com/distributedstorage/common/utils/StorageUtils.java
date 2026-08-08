package com.distributedstorage.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for storage operations including hashing, chunking, and deduplication
 */
public class StorageUtils {

    public static final int DEFAULT_CHUNK_SIZE = 4 * 1024 * 1024; // 4MB chunks

    /**
     * Calculate SHA-256 hash of byte array for content-based deduplication
     */
    public static String calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Split data into chunks for distributed storage
     */
    public static List<byte[]> chunkData(byte[] data, int chunkSize) {
        if (data == null || data.length == 0) {
            return Arrays.asList(new byte[0]);
        }

        int numChunks = (int) Math.ceil((double) data.length / chunkSize);
        return java.util.stream.IntStream.range(0, numChunks)
                .mapToObj(i -> {
                    int start = i * chunkSize;
                    int end = Math.min(start + chunkSize, data.length);
                    return Arrays.copyOfRange(data, start, end);
                })
                .toList();
    }

    /**
     * Generate unique storage node ID
     */
    public static String generateNodeId() {
        return "node-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate unique file ID
     */
    public static String generateFileId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate unique chunk ID
     */
    public static String generateChunkId() {
        return "chunk-" + UUID.randomUUID().toString().substring(0, 8);
    }
}