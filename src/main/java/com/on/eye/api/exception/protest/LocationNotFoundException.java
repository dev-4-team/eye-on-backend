package com.on.eye.api.exception.protest;

import com.on.eye.api.exception.CustomCodeException;
import com.on.eye.api.exception.ProtestErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocationNotFoundException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new LocationNotFoundException();

    /**
     * Constructs a new LocationNotFoundException with a predefined error code.
     *
     * <p>This constructor initializes the exception by invoking the superclass constructor with
     * the error code {@code ProtestErrorCode.LOCATION_NOT_FOUND} and immediately logs the associated error message.</p>
     */
    private LocationNotFoundException() {
        super(ProtestErrorCode.LOCATION_NOT_FOUND);
        log.error(ProtestErrorCode.LOCATION_NOT_FOUND.getMessage());
    }
}
