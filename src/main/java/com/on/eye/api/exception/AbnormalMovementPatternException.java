package com.on.eye.api.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbnormalMovementPatternException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new AbnormalMovementPatternException();

    private AbnormalMovementPatternException() {
        super(ProtestErrorCode.ABNORMAL_MOVEMENT_PATTERN);
        log.error(ProtestErrorCode.ABNORMAL_MOVEMENT_PATTERN.getMessage());
    }
}
