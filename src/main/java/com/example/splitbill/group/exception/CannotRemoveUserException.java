package com.example.splitbill.group.exception;

public class CannotRemoveUserException extends RuntimeException {
    private String message;

    public CannotRemoveUserException(String message) {
        super(message);
    }
}