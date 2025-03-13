package com.on.eye.api.protest.error.exception;

import com.on.eye.api.global.error.exception.CustomCodeException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbnormalMovementPatternException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new AbnormalMovementPatternException();

    private AbnormalMovementPatternException() {
        super(ProtestErrorCode.ABNORMAL_MOVEMENT_PATTERN);
        log.error(ProtestErrorCode.ABNORMAL_MOVEMENT_PATTERN.getMessage());
    }
}
