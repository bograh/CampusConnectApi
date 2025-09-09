package com.campusconnect.api.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String errorCode;
    private String message;
    private int statusCode;
    private String timestamp = LocalDateTime.now().toString();

    public ErrorResponse(String errorCode, String message, int statusCode) {
        this.errorCode = errorCode;
        this.message = message;
        this.statusCode = statusCode;
        this.timestamp = LocalDateTime.now().toString();
    }
}
