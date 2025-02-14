package com.on.eye.api.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtestNotFoundException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new ProtestNotFoundException();

    private ProtestNotFoundException() {
        super(ProtestErrorCode.PROTEST_NOT_FOUND);
        log.warn("존재하지 않는 시위에 접근 시도");
    }
}
