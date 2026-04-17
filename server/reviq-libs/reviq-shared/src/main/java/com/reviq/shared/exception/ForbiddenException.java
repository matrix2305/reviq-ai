package com.reviq.shared.exception;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String errorCode, String message) {
        super(errorCode, message);
    }
}
