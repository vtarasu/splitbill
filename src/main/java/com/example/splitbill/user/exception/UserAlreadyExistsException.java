package com.example.splitbill.user.exception;

public class UserAlreadyExistsException extends RuntimeException {
    private String message;

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
