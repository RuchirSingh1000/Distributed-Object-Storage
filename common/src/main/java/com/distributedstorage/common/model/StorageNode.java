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
    private String status; // ACTIVE, INACTIVE, MAINTENANCE, LEADER
    private long capacityBytes;
    private long usedBytes;
    private int priority;
    private long lastHeartbeat; // Timestamp of last heartbeat
    private boolean isLeader; // Flag for leader election
    private int term; // Term for leader election (Raft-style)
    private String votedFor; // Node ID that this node voted for in current term
}