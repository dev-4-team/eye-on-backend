package com.on.eye.api.global.error.exception;

public interface BaseErrorCode {
    ErrorReason getErrorReason();

    String getExplainError() throws NoSuchFieldException;
}
