package com.on.eye.api.exception;

public class InvalidTokenException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new InvalidTokenException();

    private InvalidTokenException() {
        super(GlobalErrorCode.INVALID_TOKEN);
    }
}
