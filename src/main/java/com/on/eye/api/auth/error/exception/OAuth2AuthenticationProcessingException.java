package com.on.eye.api.auth.error.exception;

import com.on.eye.api.global.error.exception.CustomCodeException;
import com.on.eye.api.global.error.exception.GlobalErrorCode;

public class OAuth2AuthenticationProcessingException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION =
            new OAuth2AuthenticationProcessingException();

    private OAuth2AuthenticationProcessingException() {
        super(GlobalErrorCode.ARGUMENT_NOT_VALID_ERROR);
    }
}
