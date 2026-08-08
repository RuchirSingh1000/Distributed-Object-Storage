package com.distributedstorage.replication.service;

import com.distributedstorage.common.model.FileChunk;
import com.distributedstorage.common.model.StorageNode;
import com.distributedstorage.common.service.LeaderElectionService;
import com.distributedstorage.common.utils.StorageUtils;
import com.distributedstorage.replication.model.ReplicationTask;
import com.distributedstorage.replication.model.ReplicationTaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Service for handling file replication with 3-way replication strategy
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReplicationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RestTemplate restTemplate;
    private final LeaderElectionService leaderElectionService;

    @Value("${replication.factor}")
    private int replicationFactor; // Default 3 for 3-way replication

    @Value("${storage.nodes}")
    private List<String> storageNodeUrls; // List of storage node URLs

    // In-memory tracking of replication tasks (in production, use database or distributed cache)
    private final Map<String, ReplicationTask> replicationTasks = new ConcurrentHashMap<>();
    private final ExecutorService replicationExecutor = Executors.newFixedThreadPool(10);

    /**
     * Handle file uploaded event from Kafka and initiate replication
     */
    @KafkaListener(topics = "${kafka.topic.file-events}", groupId = "replication-group")
    public void handleFileUploadedEvent(String event) {
        log.info("Received file event: {}", event);
        // Parse event and extract fileId
        // In a real implementation, we would parse JSON properly
        String fileId = extractFileIdFromEvent(event);
        if (fileId != null) {
            initiateReplication(fileId);
        }
    }

    /**
     * Initiate 3-way replication for a file
     */
    public void initiateReplication(String fileId) {
        log.info("Initiating replication for file: {}", fileId);

        // Check if replication already in progress
        if (replicationTasks.containsKey(fileId) &&
                replicationTasks.get(fileId).getStatus() == ReplicationTaskStatus.IN_PROGRESS) {
            log.warn("Replication already in progress for file: {}", fileId);
            return;
        }

        // Create replication task
        ReplicationTask task = new ReplicationTask();
        task.setFileId(fileId);
        task.setStatus(ReplicationTaskStatus.IN_PROGRESS);
        task.setCreatedAt(System.currentTimeMillis());
        replicationTasks.put(fileId, task);

        // Execute replication asynchronously
        replicationExecutor.submit(() -> executeReplication(task));
    }

    /**
     * Execute the actual replication process
     */
    private void executeReplication(ReplicationTask task) {
        String fileId = task.getFileId();
        log.info("Starting replication execution for file: {}", fileId);

        try {
            // Get list of storage nodes
            List<StorageNode> storageNodes = getStorageNodes();

            if (storageNodes.size() < replicationFactor) {
                log.error("Not enough storage nodes available. Need at least {} but found {}",
                        replicationFactor, storageNodes.size());
                task.setStatus(ReplicationTaskStatus.FAILED);
                task.setErrorMessage("Not enough storage nodes available");
                return;
            }

            // Select nodes for replication (simple strategy: first N nodes)
            // In production, consider network topology, load, etc.
            List<StorageNode> targetNodes = storageNodes.stream()
                    .limit(replicationFactor)
                    .collect(Collectors.toList());

            // Simulate replication to each node
            List<CompletableFuture<Void>> futures = targetNodes.stream()
                    .map(node -> CompletableFuture.runAsync(() -> {
                        try {
                            replicateToNode(fileId, node);
                            log.info("Successfully replicated file {} to node {}", fileId, node.getNodeId());
                        } catch (Exception e) {
                            log.error("Failed to replicate file {} to node {}: {}",
                                    fileId, node.getNodeId(), e.getMessage());
                            throw e;
                        }
                    }, replicationExecutor))
                    .collect(Collectors.toList());

            // Wait for all replications to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .exceptionally(ex -> {
                        log.error("Replication failed for file {}: {}", fileId, ex.getMessage());
                        task.setStatus(ReplicationTaskStatus.FAILED);
                        task.setErrorMessage(ex.getMessage());
                        return null;
                    })
                    .thenRun(() -> {
                        task.setStatus(ReplicationTaskStatus.COMPLETED);
                        task.setCompletedAt(System.currentTimeMillis());
                        log.info("Replication completed for file: {}", fileId);
                    })
                    .join(); // Wait for completion

        } catch (Exception e) {
            log.error("Error during replication process for file {}: {}", fileId, e.getMessage());
            task.setStatus(ReplicationTaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
        }
    }

    /**
     * Replicate file to a specific storage node
     */
    private void replicateToNode(String fileId, StorageNode node) {
        log.info("Replicating file {} to node {} at {}", fileId, node.getNodeId(), node.getHost());

        // In a real implementation, we would:
        // 1. Retrieve file chunks from metadata service
        // 2. Transfer each chunk to the target node
        // 3. Verify replication success

        // For now, simulate the replication process
        try {
            // Simulate network delay
            Thread.sleep(100 + (long)(Math.random() * 400)); // 100-500ms

            // Simulate occasional failure for testing (5% failure rate)
            if (Math.random() < 0.05) {
                throw new RuntimeException("Simulated network failure");
            }

            // Call storage node's replication endpoint
            String url = String.format("http://%s:%d/api/v1/replication/receive/%s",
                    node.getHost(), node.getPort(), fileId);
            // restTemplate.postForEntity(url, null, Void.class); // Would be actual call

            log.debug("Replication request sent to {} for file {}", url, fileId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Replication interrupted", e);
        }
    }

    /**
     * Get list of available storage nodes
     * In production, this would come from service discovery (Eureka)
     */
    private List<StorageNode> getStorageNodes() {
        // For demo, create mock nodes based on configured URLs
        List<StorageNode> nodes = new ArrayList<>();
        for (int i = 0; i < storageNodeUrls.size(); i++) {
            StorageNode node = new StorageNode();
            node.setNodeId("storage-node-" + (i + 1));
            node.setHost("localhost"); // Simplified
            node.setPort(8082 + i); // Different ports for each node
            node.setStatus("ACTIVE");
            node.setCapacityBytes(100L * 1024 * 1024 * 1024); // 100GB
            node.setUsedBytes((long)(Math.random() * 50 * 1024 * 1024 * 1024)); // Random usage
            node.setPriority(10 - i); // Higher priority for lower indices
            node.setLastHeartbeat(System.currentTimeMillis());
            node.setLeader(i == 0); // First node as leader initially
            node.setTerm(1);
            nodes.add(node);
        }
        return nodes;
    }

    /**
     * Extract file ID from Kafka event (simplified)
     */
    private String extractFileIdFromEvent(String event) {
        // Simple extraction - in reality use JSON parser
        if (event.contains("fileId")) {
            int start = event.indexOf("\"fileId\":\"") + "\"fileId\":\"".length();
            int end = event.indexOf("\"", start);
            if (start > end && end > 0) {
                return event.substring(start, end);
            }
        }
        return null;
    }

    /**
     * Get replication task status
     */
    public ReplicationTask getReplicationStatus(String fileId) {
        return replicationTasks.get(fileId);
    }

    /**
     * Get all replication tasks
     */
    public Map<String, ReplicationTask> getAllReplicationTasks() {
        return Collections.unmodifiableMap(replicationTasks);
    }

    /**
     * Shutdown executor service
     */
    public void shutdown() {
        replicationExecutor.shutdown();
        try {
            if (!replicationExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                replicationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            replicationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}