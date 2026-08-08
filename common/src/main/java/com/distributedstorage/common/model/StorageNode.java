package com.distributedstorage.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a storage node in the distributed storage cluster
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageNode {
    private String nodeId;
    private String host;
    private int port;
    private String status; // ACTIVE, INACTIVE, MAINTENANCE
    private long capacityBytes;
    private long usedBytes;
    private int priority;
}