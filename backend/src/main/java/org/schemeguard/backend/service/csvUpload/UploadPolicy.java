package org.schemeguard.backend.service.csvUpload;

import org.schemeguard.backend.config.UploadProperties;
import org.schemeguard.backend.exception.UploadException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class UploadPolicy {

    private final UploadProperties properties;

    public UploadPolicy(UploadProperties properties) {
        this.properties = properties;
    }

    public int calculateTotalParts(long fileSizeBytes) {
        long totalParts = (fileSizeBytes + properties.getChunkSizeBytes() - 1)
                / properties.getChunkSizeBytes();
        if (totalParts > properties.getMaxParts()) {
            throw new UploadException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "TOO_MANY_PARTS",
                    "The file would require too many upload parts."
            );
        }
        return Math.toIntExact(totalParts);
    }

    public void validateCreateRequest(String fileName, long fileSizeBytes) {
        if (fileSizeBytes <= 0 || fileSizeBytes > properties.getMaxFileSizeBytes()) {
            throw new UploadException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "FILE_SIZE_NOT_ALLOWED",
                    "The file size is outside the allowed range."
            );
        }

        if (fileName.contains("/") || fileName.contains("\\")
                || !fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new UploadException(
                    HttpStatus.BAD_REQUEST,
                    "FILE_NAME_NOT_ALLOWED",
                    "Only a simple .csv file name is accepted."
            );
        }

        calculateTotalParts(fileSizeBytes);
    }

    public void validatePartNumber(int partNumber, int totalParts) {
        if (partNumber < 0 || partNumber >= totalParts) {
            throw new UploadException(
                    HttpStatus.BAD_REQUEST,
                    "PART_NUMBER_NOT_ALLOWED",
                    "The part number is outside the upload range."
            );
        }
    }

    public long expectedPartSize(long fileSizeBytes, int partNumber) {
        long start = partNumber * properties.getChunkSizeBytes();
        long remaining = fileSizeBytes - start;
        return Math.min(properties.getChunkSizeBytes(), remaining);
    }

    public long getChunkSizeBytes() {
        return properties.getChunkSizeBytes();
    }

    public int getMaxParallelUploads() {
        return properties.getMaxParallelUploads();
    }
}
