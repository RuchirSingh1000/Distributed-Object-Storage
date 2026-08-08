package com.distributedstorage.common.service;

import com.distributedstorage.common.model.StorageNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for leader election and failure detection using heartbeats
 * Implements a simplified Raft-inspired consensus algorithm
 */
@Service
@Slf4j
public class LeaderElectionService {

    private static final long HEARTBEAT_TIMEOUT_MS = 5000; // 5 seconds
    private static final long ELECTION_TIMEOUT_MIN = 15000; // 15 seconds
    private static final long ELECTION_TIMEOUT_MAX = 30000; // 30 seconds

    /**
     * Check if a node is considered active based on last heartbeat
     */
    public boolean isNodeActive(StorageNode node) {
        long timeSinceLastHeartbeat = System.currentTimeMillis() - node.getLastHeartbeat();
        return timeSinceLastHeartbeat < HEARTBEAT_TIMEOUT_MS;
    }

    /**
     * Determine if election timeout has elapsed for a node
     */
    public boolean isElectionTimeoutElapsed(StorageNode node) {
        long timeSinceLastHeartbeat = System.currentTimeMillis() - node.getLastHeartbeat();
        long electionTimeout = ELECTION_TIMEOUT_MIN +
                (long)(Math.random() * (ELECTION_TIMEOUT_MAX - ELECTION_TIMEOUT_MIN));
        return timeSinceLastHeartbeat > electionTimeout;
    }

    /**
     * Find the node with highest priority that is active
     */
    public Optional<StorageNode> findLeaderCandidate(List<StorageNode> nodes) {
        return nodes.stream()
                .filter(this::isNodeActive)
                .filter(node -> !node.isLeader()) // Only consider non-leaders for election
                .max((n1, n2) -> {
                    // Higher priority wins, if equal then higher term wins, if equal then lower nodeId wins
                    int priorityCompare = Integer.compare(n2.getPriority(), n1.getPriority());
                    if (priorityCompare != 0) return priorityCompare;

                    int termCompare = Integer.compare(n2.getTerm(), n1.getTerm());
                    if (termCompare != 0) return termCompare;

                    return n1.getNodeId().compareTo(n2.getNodeId());
                });
    }

    /**
     * Initiate leader election process
     */
    public void initiateElection(List<StorageNode> nodes, StorageNode currentNode) {
        log.info("Node {} initiating election", currentNode.getNodeId());

        // Increment term and vote for self
        currentNode.setTerm(currentNode.getTerm() + 1);
        currentNode.setVotedFor(currentNode.getNodeId());
        currentNode.setLeader(false);

        // Find potential leader among other nodes
        Optional<StorageNode> leaderCandidate = findLeaderCandidate(nodes);

        if (leaderCandidate.isPresent()) {
            StorageNode candidate = leaderCandidate.get();
            // Vote for the candidate if it has higher priority/term
            if (hasHigherPriority(candidate, currentNode)) {
                currentNode.setVotedFor(candidate.getNodeId());
                log.info("Node {} voting for node {}", currentNode.getNodeId(), candidate.getNodeId());
            } else {
                // Consider self as leader if we have equal or better credentials
                currentNode.setLeader(true);
                currentNode.setVotedFor(currentNode.getNodeId());
                log.info("Node {} declaring itself as leader", currentNode.getNodeId());
            }
        } else {
            // No other active nodes, become leader
            currentNode.setLeader(true);
            currentNode.setVotedFor(currentNode.getNodeId());
            log.info("Node {} declaring itself as leader (no competition)", currentNode.getNodeId());
        }
    }

    private boolean hasHigherPriority(StorageNode n1, StorageNode n2) {
        if (n1.getPriority() != n2.getPriority()) {
            return n1.getPriority() > n2.getPriority();
        }
        if (n1.getTerm() != n2.getTerm()) {
            return n1.getTerm() > n2.getTerm();
        }
        return n1.getNodeId().compareTo(n2.getNodeId()) < 0; // Lower nodeId wins tie
    }

    /**
     * Update heartbeat timestamp for a node
     */
    public void updateHeartbeat(StorageNode node) {
        node.setLastHeartbeat(System.currentTimeMillis());
    }
}