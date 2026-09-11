package org.schemeguard.backend.dto.Upload;

import java.util.UUID;

public record CreateUploadResponse(
        UUID uploadId,
        long chunkSizeBytes,
        int totalParts,
        int maxParallelUploads
) {
}
