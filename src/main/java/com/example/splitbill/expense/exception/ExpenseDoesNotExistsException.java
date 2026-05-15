package com.example.splitbill.expense.exception;

public class ExpenseDoesNotExistsException extends RuntimeException {
    private String message;

    public ExpenseDoesNotExistsException(String message) {
        super(message);
    }
}
