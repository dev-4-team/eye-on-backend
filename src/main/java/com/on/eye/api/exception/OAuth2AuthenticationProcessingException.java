package com.on.eye.api.exception;

public class OAuth2AuthenticationProcessingException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION =
            new OAuth2AuthenticationProcessingException();

    private OAuth2AuthenticationProcessingException() {
        super(GlobalErrorCode.ARGUMENT_NOT_VALID_ERROR);
    }
}
