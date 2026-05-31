package com.example.splitbill.group.exception;

public class CannotRemoveUserException extends Throwable {
    private String message;

    public CannotRemoveUserException(String message) {
        super(message);
    }
}