package com.on.eye.api.exception;

public interface BaseErrorCode {
    ErrorReason getErrorReason();

    String getExplainError() throws NoSuchFieldException;
}
