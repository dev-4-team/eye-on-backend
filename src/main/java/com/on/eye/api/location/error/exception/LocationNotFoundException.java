package com.on.eye.api.location.error.exception;

import com.on.eye.api.global.error.exception.CustomCodeException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocationNotFoundException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new LocationNotFoundException();

    private LocationNotFoundException() {
        super(LocationErrorCode.LOCATION_NOT_FOUND);
        log.error(LocationErrorCode.LOCATION_NOT_FOUND.getMessage());
    }
}
