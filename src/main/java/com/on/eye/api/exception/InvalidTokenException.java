package com.on.eye.api.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidTokenException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new InvalidTokenException();

    private InvalidTokenException() {
        super(GlobalErrorCode.INVALID_TOKEN);
        log.warn("유효하지 않은 토큰 사용 시도");
    }
}
