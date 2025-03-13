package com.on.eye.api.auth.error.exception;

import com.on.eye.api.global.error.exception.CustomCodeException;
import com.on.eye.api.protest.error.exception.ProtestErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OutOfValidProtestRangeException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new OutOfValidProtestRangeException();

    private OutOfValidProtestRangeException() {
        super(ProtestErrorCode.OUT_OF_VALID_PROTEST_RANGE);
        log.warn("허용된 반경을 벗어난 위치에서 인증 시도");
    }
}
