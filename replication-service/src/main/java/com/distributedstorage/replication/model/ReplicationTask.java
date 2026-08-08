package com.distributedstorage.replication.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a replication task for tracking file replication progress
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplicationTask {
    private String fileId;
    private ReplicationTaskStatus status;
    private long createdAt;
    private long completedAt;
    private String errorMessage;
}