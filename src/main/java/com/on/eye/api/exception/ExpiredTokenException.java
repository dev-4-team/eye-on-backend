package com.on.eye.api.exception;

public class ExpiredTokenException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new ExpiredTokenException();

    private ExpiredTokenException() {
        super(GlobalErrorCode.TOKEN_EXPIRED);
    }
}
