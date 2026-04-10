package com.sporty.feeds.exception;

public class InvalidFeedException extends RuntimeException {

    private final Object requestContext;

    public InvalidFeedException(String message, Object requestContext) {
        super(message);
        this.requestContext = requestContext;
    }

    public Object getRequestContext() {
        return requestContext;
    }
}
