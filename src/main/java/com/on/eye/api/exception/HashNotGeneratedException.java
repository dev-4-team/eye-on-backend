package com.on.eye.api.exception;

import static com.on.eye.api.exception.GlobalErrorCode.INVALID_HASH_GENERATION;

public class HashNotGeneratedException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new HashNotGeneratedException();

    private HashNotGeneratedException() {
        super(INVALID_HASH_GENERATION);
    }
}
