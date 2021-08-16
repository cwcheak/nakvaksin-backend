package org.nakvaksin.service.exception;

public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(String msg) {
        super(msg);
    }
}
