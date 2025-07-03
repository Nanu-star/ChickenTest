package com.chickentest.exception;

public class MaxStockExceededException extends RuntimeException {
    public MaxStockExceededException(String message) {
        super(message);
    }

    public MaxStockExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
