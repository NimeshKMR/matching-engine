package com.nimeshkmr.core;

public class UnauthorizedOrderCancellationException extends RuntimeException {
    public UnauthorizedOrderCancellationException(String message) {
        super(message);
    }
}
