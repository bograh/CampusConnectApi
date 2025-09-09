package com.campusconnect.api.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationException extends RuntimeException {
    private final Map<String, Object> details;

    public ValidationException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }

}
