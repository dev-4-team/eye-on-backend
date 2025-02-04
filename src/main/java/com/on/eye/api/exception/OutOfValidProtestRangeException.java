package com.on.eye.api.exception;

public class OutOfValidProtestRangeException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new OutOfValidProtestRangeException();

    private OutOfValidProtestRangeException() {
        super(ProtestErrorCode.OUT_OF_VALID_PROTEST_RANGE);
    }
}
