package com.distributedstorage.metadata.controller;

import com.distributedstorage.metadata.model.ChunkMetadata;
import com.distributedstorage.metadata.service.ChunkMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for chunk metadata operations
 */
@RestController
@RequestMapping("/api/v1/chunks")
@RequiredArgsConstructor
public class ChunkMetadataController {
    private final ChunkMetadataService chunkMetadataService;

    @PostMapping
    public ResponseEntity<ChunkMetadata> saveChunkMetadata(@RequestBody ChunkMetadata chunkMetadata) {
        ChunkMetadata saved = chunkMetadataService.saveChunkMetadata(chunkMetadata);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/checksum/{checksum}")
    public ResponseEntity<Optional<ChunkMetadata>> findChunkByChecksum(@PathVariable String checksum) {
        Optional<ChunkMetadata> chunk = chunkMetadataService.findChunkByChecksum(checksum);
        return ResponseEntity.ok(chunk);
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<ChunkMetadata>> findChunksByFileId(@PathVariable String fileId) {
        List<ChunkMetadata> chunks = chunkMetadataService.findChunksByFileId(fileId);
        return ResponseEntity.ok(chunks);
    }

    @DeleteMapping("/{chunkId}")
    public ResponseEntity<Void> deleteChunkMetadata(@PathVariable String chunkId) {
        chunkMetadataService.deleteChunkMetadata(chunkId);
        return ResponseEntity.noContent().build();
    }
}