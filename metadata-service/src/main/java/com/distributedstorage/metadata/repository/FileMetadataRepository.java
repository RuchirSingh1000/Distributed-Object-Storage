package com.distributedstorage.metadata.repository;

import com.distributedstorage.metadata.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for FileMetadata entities
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByFileId(String fileId);
}