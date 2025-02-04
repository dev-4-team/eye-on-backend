package com.on.eye.api.dto;

import java.time.LocalDateTime;

import com.on.eye.api.exception.ErrorReason;

public record ErrorResponse(
        boolean success,
        String message,
        int status,
        String code,
        LocalDateTime timestamp,
        String path) {
    // 주 생성자에서 success와 timestamp 초기화
    public ErrorResponse(ErrorReason errorReason, String path) {
        this(
                false,
                errorReason.getMessage(),
                errorReason.getStatus(),
                errorReason.getCode(),
                LocalDateTime.now(),
                path);
    }

    public ErrorResponse(int status, String code, String message, String path) {
        this(false, message, status, code, LocalDateTime.now(), path);
    }
}
