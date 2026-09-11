package org.schemeguard.backend.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiError(
        int status,
        String message,
                OffsetDateTime timestamp,
                String errorCode,
                Map<String, String> fieldErrors
) {
        public ApiError(int status, String message, OffsetDateTime timestamp) {
                this(status, message, timestamp, null, null);
        }
}