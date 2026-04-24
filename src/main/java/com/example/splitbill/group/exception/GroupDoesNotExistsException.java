package com.example.splitbill.group.exception;

public class GroupDoesNotExistsException extends RuntimeException {
    private String message;

    public GroupDoesNotExistsException(String message) {
        super(message);
    }
}
