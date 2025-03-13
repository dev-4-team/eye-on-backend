package com.on.eye.api.auth.error.exception;

import com.on.eye.api.global.error.exception.CustomCodeException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlreadyDeletedUserException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new AlreadyDeletedUserException();

    private AlreadyDeletedUserException() {
        super(UserErrorCode.USER_ALREADY_DELETED);
        log.warn("이미 삭제된 사용자 접근 시도");
    }
}
