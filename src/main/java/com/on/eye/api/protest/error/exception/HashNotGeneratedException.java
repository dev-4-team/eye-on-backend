package com.on.eye.api.protest.error.exception;

import static com.on.eye.api.global.error.exception.GlobalErrorCode.INVALID_HASH_GENERATION;

import com.on.eye.api.global.error.exception.CustomCodeException;

public class HashNotGeneratedException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new HashNotGeneratedException();

    private HashNotGeneratedException() {
        super(INVALID_HASH_GENERATION);
    }
}
