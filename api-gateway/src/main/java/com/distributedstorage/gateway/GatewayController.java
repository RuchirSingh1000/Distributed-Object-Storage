package com.distributedstorage.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Map;

/**
 * API Gateway controller that forwards requests to the appropriate backend services.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GatewayController {

    private final RestTemplate restTemplate;

    // Service URLs
    @Value("${gateway.metadata-service-url}")
    private String metadataServiceUrl;

    @Value("${gateway.storage-service-url}")
    private String storageServiceUrl;

    // Metadata service endpoints
    @GetMapping("/metadata")
    public ResponseEntity<Object> getAllMetadata(@RequestParam Map<String, String> queryParams,
                                                 @RequestHeader HttpHeaders headers) {
        return forwardGet(metadataServiceUrl + "/api/v1/metadata", queryParams, headers);
    }

    @PostMapping("/metadata")
    public ResponseEntity<Object> createMetadata(@RequestParam Map<String, String> queryParams,
                                                 @RequestHeader HttpHeaders headers,
                                                 @RequestBody String body) {
        return forwardPost(metadataServiceUrl + "/api/v1/metadata", queryParams, headers, body);
    }

    @GetMapping("/metadata/{fileId}")
    public ResponseEntity<Object> getMetadata(@PathVariable String fileId,
                                              @RequestParam Map<String, String> queryParams,
                                              @RequestHeader HttpHeaders headers) {
        return forwardGet(metadataServiceUrl + "/api/v1/metadata/" + fileId, queryParams, headers);
    }

    @PutMapping("/metadata/{fileId}/status")
    public ResponseEntity<Object> updateMetadataStatus(@PathVariable String fileId,
                                                       @RequestParam Map<String, String> queryParams,
                                                       @RequestHeader HttpHeaders headers,
                                                       @RequestParam String status) {
        // We'll put the status as a query parameter
        queryParams.put("status", status);
        return forwardPut(metadataServiceUrl + "/api/v1/metadata/" + fileId, queryParams, headers);
    }

    @DeleteMapping("/metadata/{fileId}")
    public ResponseEntity<Object> deleteMetadata(@PathVariable String fileId,
                                                 @RequestParam Map<String, String> queryParams,
                                                 @RequestHeader HttpHeaders headers) {
        return forwardDelete(metadataServiceUrl + "/api/v1/metadata/" + fileId, queryParams, headers);
    }

    // Storage service endpoints
    @PostMapping("/storage/upload")
    public ResponseEntity<Object> uploadFile(@RequestParam Map<String, String> queryParams,
                                             @RequestHeader HttpHeaders headers,
                                             @RequestPart("file") MultipartFile file) {
        // Prepare the body for the multipart request
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        // Prepare headers: set content type to multipart/form-data and copy original headers
        HttpHeaders forwardedHeaders = new HttpHeaders();
        forwardedHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        forwardedHeaders.addAll(headers);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, forwardedHeaders);

        // Build the URL for the storage service
        StringBuilder fullUrl = new StringBuilder(storageServiceUrl);
        fullUrl.append("/api/v1/storage/upload");
        if (!queryParams.isEmpty()) {
            fullUrl.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    fullUrl.append("&");
                }
                fullUrl.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }

        // Forward the request to the storage service
        ResponseEntity<Object> response = restTemplate.exchange(
                fullUrl.toString(),
                HttpMethod.POST,
                requestEntity,
                Object.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }

    @GetMapping("/storage/download/{fileId}")
    public ResponseEntity<Object> downloadFile(@PathVariable String fileId,
                                               @RequestParam Map<String, String> queryParams,
                                               @RequestHeader HttpHeaders headers) {
        return forwardGet(storageServiceUrl + "/api/v1/storage/download/" + fileId, queryParams, headers);
    }

    @DeleteMapping("/storage/delete/{fileId}")
    public ResponseEntity<Object> deleteFile(@PathVariable String fileId,
                                             @RequestParam Map<String, String> queryParams,
                                             @RequestHeader HttpHeaders headers) {
        return forwardDelete(storageServiceUrl + "/api/v1/storage/delete/" + fileId, queryParams, headers);
    }

    // Helper methods for forwarding
    private ResponseEntity<Object> forwardGet(String url, Map<String, String> queryParams, HttpHeaders headers) {
        // Build the full URL with query parameters
        StringBuilder fullUrl = new StringBuilder(url);
        if (!queryParams.isEmpty()) {
            fullUrl.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    fullUrl.append("&");
                }
                fullUrl.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Object> response = restTemplate.exchange(
                fullUrl.toString(),
                HttpMethod.GET,
                entity,
                Object.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }

    private ResponseEntity<Object> forwardPost(String url, Map<String, String> queryParams, HttpHeaders headers, String body) {
        StringBuilder fullUrl = new StringBuilder(url);
        if (!queryParams.isEmpty()) {
            fullUrl.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    fullUrl.append("&");
                }
                fullUrl.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Object> response = restTemplate.exchange(
                fullUrl.toString(),
                HttpMethod.POST,
                entity,
                Object.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }

    private ResponseEntity<Object> forwardPut(String url, Map<String, String> queryParams, HttpHeaders headers) {
        StringBuilder fullUrl = new StringBuilder(url);
        if (!queryParams.isEmpty()) {
            fullUrl.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    fullUrl.append("&");
                }
                fullUrl.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Object> response = restTemplate.exchange(
                fullUrl.toString(),
                HttpMethod.PUT,
                entity,
                Object.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }

    private ResponseEntity<Object> forwardDelete(String url, Map<String, String> queryParams, HttpHeaders headers) {
        StringBuilder fullUrl = new StringBuilder(url);
        if (!queryParams.isEmpty()) {
            fullUrl.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    fullUrl.append("&");
                }
                fullUrl.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Object> response = restTemplate.exchange(
                fullUrl.toString(),
                HttpMethod.DELETE,
                entity,
                Object.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }
}