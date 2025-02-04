package com.on.eye.api.exception;

public class ProtestNotFoundException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new ProtestNotFoundException();

    private ProtestNotFoundException() {
        super(ProtestErrorCode.PROTEST_NOT_FOUND);
    }
}
