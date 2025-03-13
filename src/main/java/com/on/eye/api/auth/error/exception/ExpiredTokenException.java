package com.on.eye.api.auth.error.exception;

import com.on.eye.api.global.error.exception.CustomCodeException;
import com.on.eye.api.global.error.exception.GlobalErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExpiredTokenException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new ExpiredTokenException();

    private ExpiredTokenException() {
        super(GlobalErrorCode.TOKEN_EXPIRED);
        log.warn("만료된 토큰 사용 시도");
    }
}
