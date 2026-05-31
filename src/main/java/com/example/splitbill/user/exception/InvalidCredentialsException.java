package com.example.splitbill.user.exception;

public class InvalidCredentialsException extends RuntimeException {
    private String message;

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
