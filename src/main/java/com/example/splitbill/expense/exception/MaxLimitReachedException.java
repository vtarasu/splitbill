package com.example.splitbill.expense.exception;

public class MaxLimitReachedException extends RuntimeException {
    private String message;

    public MaxLimitReachedException(String message) {
        super(message);
    }
}
