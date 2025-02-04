package com.on.eye.api.exception;

public class DuplicateVerificationException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new DuplicateVerificationException();

    private DuplicateVerificationException() {
        super(ProtestErrorCode.DUPLICATED_VERIFICATION);
    }
}
