package com.distributedstorage.replication.model;

/**
 * Status of a replication task
 */
public enum ReplicationTaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}