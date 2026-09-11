package org.schemeguard.backend.dto.Upload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateUploadRequest(
        @NotBlank
        @Size(max = 255)
        String fileName,

        @NotNull
        @Positive
        Long fileSizeBytes,

        @Pattern(
                regexp = "(?i)^[a-f0-9]{64}$",
                message = "fileSha256 must be a SHA-256 hex string"
        )
        String fileSha256
) {
}
