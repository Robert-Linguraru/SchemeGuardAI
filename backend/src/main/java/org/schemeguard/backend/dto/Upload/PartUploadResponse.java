package org.schemeguard.backend.dto.Upload;

public record PartUploadResponse(
        int partNumber,
        long sizeBytes,
        String sha256,
        boolean alreadyUploaded
) {
}
