package org.schemeguard.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDatabaseError(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                request.getRequestURI().startsWith("/api/rule")
                        ? "A rule with this code already exists."
                        : "The request could not be completed."
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationError(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                request.getRequestURI().startsWith("/api/rule")
                        ? "Some rule fields are invalid. Please review the form."
                        : "Invalid request data"
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException exception
    ) {
        String errorCode = exception.getMessage().startsWith("Card scheme")
                ? "CARD_SCHEME_NOT_FOUND"
                : "RULE_NOT_FOUND";
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), errorCode);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(
            ConflictException exception
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
            UnauthorizedException exception
    ) {
        return buildError(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message
    ) {
        return buildError(status, message, null);
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            String errorCode
    ) {
        return ResponseEntity
                .status(status)
                .body(new ApiError(
                        status.value(),
                        message,
                        OffsetDateTime.now(),
                        errorCode,
                        null
                ));
    }
}