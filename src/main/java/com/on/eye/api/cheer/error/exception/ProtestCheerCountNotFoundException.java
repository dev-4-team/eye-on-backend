package com.on.eye.api.cheer.error.exception;

import com.on.eye.api.global.error.exception.CustomCodeException;
import com.on.eye.api.protest.error.exception.ProtestErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtestCheerCountNotFoundException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new ProtestCheerCountNotFoundException();

    private ProtestCheerCountNotFoundException() {
        super(ProtestErrorCode.PROTEST_CHEER_COUNT_NOT_FOUND);
        log.warn("Cache 존재하지 않는 시위에 접근 시도");
    }
}
