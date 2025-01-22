package com.on.eye.api.exception;

public class AlreadyDeletedUserException extends CustomCodeException {
    public static final CustomCodeException EXCEPTION = new AlreadyDeletedUserException();

    private AlreadyDeletedUserException() {
        super(UserErrorCode.USER_ALREADY_DELETED);
    }
}
