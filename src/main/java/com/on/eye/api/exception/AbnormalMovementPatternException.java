package com.on.eye.api.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbnormalMovementPatternException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new AbnormalMovementPatternException();

    /**
     * Constructs a new AbnormalMovementPatternException with a predefined error code.
     *
     * This private constructor initializes the exception using the ProtestErrorCode.ABNORMAL_MOVEMENT_PATTERN
     * and logs the corresponding error message. It prevents external instantiation, supporting the intended
     * singleton usage via the static EXCEPTION instance.
     */
    private AbnormalMovementPatternException() {
        super(ProtestErrorCode.ABNORMAL_MOVEMENT_PATTERN);
        log.error(ProtestErrorCode.ABNORMAL_MOVEMENT_PATTERN.getMessage());
    }
}
