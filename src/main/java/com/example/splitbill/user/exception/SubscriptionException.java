package com.example.splitbill.user.exception;

public class SubscriptionException extends RuntimeException {
    private String message;

    public SubscriptionException(String message) {
        super(message);
    }
}
