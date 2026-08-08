package com.distributedstorage.metadata.repository;

import com.distributedstorage.metadata.model.ChunkMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ChunkMetadata entities
 */
@Repository
public interface ChunkMetadataRepository extends JpaRepository<ChunkMetadata, Long> {
    Optional<ChunkMetadata> findByChecksum(String checksum);
    List<ChunkMetadata> findByFileId(String fileId);
    Optional<ChunkMetadata> findByChunkId(String chunkId);
}