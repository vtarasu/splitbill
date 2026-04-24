package com.example.splitbill.group.exception;

public class GroupAlreadyExistsException extends RuntimeException {
    private String message;

    public GroupAlreadyExistsException(String message) {
        super(message);
    }
}
