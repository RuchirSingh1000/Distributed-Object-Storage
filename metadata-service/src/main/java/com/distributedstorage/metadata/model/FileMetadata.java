package com.distributedstorage.metadata.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Represents file metadata stored in the PostgreSQL database
 */
@Entity
@Table(name = "file_metadata")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", unique = true, nullable = false)
    private String fileId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "upload_timestamp", nullable = false)
    private Instant uploadTimestamp;

    @Column(name = "checksum")
    private String overallChecksum; // SHA-256 of entire file

    @ElementCollection
    @CollectionTable(name = "file_chunks", joinColumns = @JoinColumn(name = "metadata_id"))
    @Column(name = "chunk_ids")
    private List<String> chunkIds;

    @Column(name = "status")
    private String status; // UPLOADED, PROCESSING, AVAILABLE, DELETED

    // New fields for chunking and deduplication
    @Column(name = "chunk_size")
    private Integer chunkSize; // Size of each chunk in bytes

    @Column(name = "total_chunks")
    private Integer totalChunks; // Total number of chunks

    @Column(name = "is_chunked")
    private Boolean isChunked; // Whether file was split into chunks
}