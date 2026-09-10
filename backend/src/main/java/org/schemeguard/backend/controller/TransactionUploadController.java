package org.schemeguard.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.schemeguard.backend.dto.Upload.CompleteUploadResponse;
import org.schemeguard.backend.dto.Upload.CreateUploadRequest;
import org.schemeguard.backend.dto.Upload.CreateUploadResponse;
import org.schemeguard.backend.dto.Upload.PartUploadResponse;
import org.schemeguard.backend.dto.Upload.UploadStatusResponse;
import org.schemeguard.backend.service.csvUpload.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class TransactionUploadController {

    private final UploadService uploadService;

    public TransactionUploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping
    public ResponseEntity<CreateUploadResponse> createUpload(
            @Valid @RequestBody CreateUploadRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(uploadService.createUpload(request));
    }

    @PutMapping(
            value = "/{uploadId}/parts/{partNumber}",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public PartUploadResponse uploadPart(
            @PathVariable UUID uploadId,
            @PathVariable int partNumber,
            @RequestHeader(value = "X-Chunk-SHA256", required = false) String clientSha256,
            HttpServletRequest request
    ) throws IOException {
        return uploadService.uploadPart(
                uploadId,
                partNumber,
                request.getInputStream(),
                clientSha256
        );
    }

    @PostMapping("/{uploadId}/complete")
    public CompleteUploadResponse completeUpload(@PathVariable UUID uploadId) {
        return uploadService.completeUpload(uploadId);
    }

    @GetMapping("/{uploadId}")
    public UploadStatusResponse getStatus(@PathVariable UUID uploadId) {
        return uploadService.getStatus(uploadId);
    }

    @DeleteMapping("/{uploadId}")
    public ResponseEntity<Void> cancelUpload(@PathVariable UUID uploadId) {
        uploadService.cancelUpload(uploadId);
        return ResponseEntity.noContent().build();
    }
}
