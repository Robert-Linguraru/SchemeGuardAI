package org.schemeguard.backend.dto.Upload;

import org.schemeguard.backend.entity.enums.UploadStatus;

import java.util.UUID;

public record UploadStatusResponse(
        UUID uploadId,
        String fileName,
        UploadStatus status,
        long expectedFileSizeBytes,
        long uploadedBytes,
        int uploadedParts,
        int expectedTotalParts,
        long processedRows,
        String errorMessage
) {
}
