package com.distributedstorage.metadata.service;

import com.distributedstorage.metadata.model.ChunkMetadata;
import com.distributedstorage.metadata.repository.ChunkMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing chunk metadata
 */
@Service
@RequiredArgsConstructor
public class ChunkMetadataService {
    private final ChunkMetadataRepository chunkMetadataRepository;

    public ChunkMetadata saveChunkMetadata(ChunkMetadata chunkMetadata) {
        return chunkMetadataRepository.save(chunkMetadata);
    }

    public Optional<ChunkMetadata> findChunkByChecksum(String checksum) {
        return chunkMetadataRepository.findByChecksum(checksum);
    }

    public List<ChunkMetadata> findChunksByFileId(String fileId) {
        return chunkMetadataRepository.findByFileId(fileId);
    }

    public void deleteChunkMetadata(String chunkId) {
        Optional<ChunkMetadata> chunkOpt = chunkMetadataRepository.findByChunkId(chunkId);
        chunkOpt.ifPresent(chunk -> chunkMetadataRepository.deleteById(chunk.getId()));
    }
}