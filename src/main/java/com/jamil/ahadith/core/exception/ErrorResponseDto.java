package com.jamil.ahadith.core.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDto (
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        String requestId,
        Map<String, String> validationErrors) {

    public ErrorResponseDto(int status, String error, String message, String path, LocalDateTime timestamp) {
        this(status, error, message, path, timestamp, null, null);
    }

    public ErrorResponseDto(
            int status,
            String error,
            String message,
            String path,
            LocalDateTime timestamp,
            String requestId) {
        this(status, error, message, path, timestamp, requestId, null);
    }
}

