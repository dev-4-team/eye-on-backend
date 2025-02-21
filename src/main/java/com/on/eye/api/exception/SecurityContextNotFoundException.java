package com.on.eye.api.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecurityContextNotFoundException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new SecurityContextNotFoundException();

    private SecurityContextNotFoundException() {
        super(GlobalErrorCode.SECURITY_CONTEXT_NOT_FOUND);
        log.error("사용자 인증 정보를 찾을 수 없음");
    }
}
