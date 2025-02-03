package com.on.eye.api.exception;

public class SecurityContextNotFoundException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new SecurityContextNotFoundException();

    private SecurityContextNotFoundException() {
        super(GlobalErrorCode.SECURITY_CONTEXT_NOT_FOUND);
    }
}
