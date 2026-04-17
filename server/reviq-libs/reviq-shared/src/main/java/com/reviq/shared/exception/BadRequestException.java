package com.reviq.shared.exception;

public class BadRequestException extends BusinessException {

    public BadRequestException(String errorCode, String message) {
        super(errorCode, message);
    }
}
