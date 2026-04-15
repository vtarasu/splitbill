package com.example.splitbill.user;

public class UserDoesNotExistsException extends RuntimeException {
    private String message;

    public UserDoesNotExistsException(String message) {
        super(message);
    }
}
