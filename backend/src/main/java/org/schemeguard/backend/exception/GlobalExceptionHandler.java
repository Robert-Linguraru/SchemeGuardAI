package org.schemeguard.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException exception
    ) {
        String exceptionMessage = exception.getMessage();

        if ("Card scheme not found".equals(exceptionMessage)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiError(
                            HttpStatus.NOT_FOUND.value(),
                            "Card scheme could not be found.",
                            OffsetDateTime.now(),
                            "CARD_SCHEME_NOT_FOUND",
                            null
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        "This rule could not be found.",
                        OffsetDateTime.now(),
                        "RULE_NOT_FOUND",
                        null
                ));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(
            ConflictException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        exception.getMessage(),
                        OffsetDateTime.now(),
                        "CONFLICT",
                        null
                ));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
            UnauthorizedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(
                        HttpStatus.UNAUTHORIZED.value(),
                        exception.getMessage(),
                        OffsetDateTime.now(),
                        "UNAUTHORIZED",
                        null
                ));
    }
}