package com.distributedstorage.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a chunk of a file stored in the distributed storage system
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileChunk {
    private String chunkId;
    private String fileId;
    private int chunkIndex;
    private long size;
    private String checksum;
    private String storageNodeId;
    private long createdAt;
}