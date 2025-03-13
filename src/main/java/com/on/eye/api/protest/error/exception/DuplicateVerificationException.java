package com.on.eye.api.protest.error.exception;

import com.on.eye.api.global.error.exception.CustomCodeException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DuplicateVerificationException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new DuplicateVerificationException();

    private DuplicateVerificationException() {
        super(ProtestErrorCode.DUPLICATED_VERIFICATION);
        log.warn("이미 인증된 시위에 대한 참가 인증 중복 시도");
    }
}
