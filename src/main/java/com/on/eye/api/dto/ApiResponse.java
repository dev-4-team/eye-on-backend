package com.on.eye.api.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.on.eye.api.exception.ErrorReason;

import lombok.Getter;

@Getter
@JsonPropertyOrder({"success", "message", "data"})
public class ApiResponse<T> {
    private boolean success = true;
    private String message;
    private T data = null;
    private LocalDateTime timestamp;
    private int status;
    private String code;
    private String path;

    public ApiResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public ApiResponse(T data) {
        this.data = data;
    }

    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public ApiResponse(String message, T data, boolean b) {
        this.message = message;
        this.data = data;
        this.success = b;
    }

    public ApiResponse(ErrorReason errorReason, String path) {
        this.message = errorReason.getMessage();
        this.status = errorReason.getStatus();
        this.code = errorReason.getCode();
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, false);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(message, data, false);
    }

    public static <T> ApiResponse<T> error(ErrorReason errorReason, String path) {
        return new ApiResponse<>(errorReason, path);
    }
}
