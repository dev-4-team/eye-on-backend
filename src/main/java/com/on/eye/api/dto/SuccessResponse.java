package com.on.eye.api.dto;

import java.time.LocalDateTime;

public record SuccessResponse<T>(boolean success, T data, int status, LocalDateTime timestamp) {
    public SuccessResponse(int status, T data) {
        this(true, data, status, LocalDateTime.now());
    }
}
