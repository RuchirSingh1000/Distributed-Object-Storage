package com.distributedstorage.metadata.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents metadata for a individual chunk stored in the distributed storage system
 */
@Entity
@Table(name = "chunk_metadata")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChunkMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chunk_id", unique = true, nullable = false)
    private String chunkId;

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "size", nullable = false)
    private long size;

    @Column(name = "checksum", nullable = false)
    private String checksum; // SHA-256 of chunk data for deduplication

    @Column(name = "storage_node_id", nullable = false)
    private String storageNodeId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "is_duplicate")
    private boolean isDuplicate; // Flag for deduplication

    @Column(name = "original_chunk_id")
    private String originalChunkId; // Points to original chunk if this is a duplicate
}