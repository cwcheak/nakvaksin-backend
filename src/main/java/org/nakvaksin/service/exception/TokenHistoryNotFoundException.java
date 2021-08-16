package org.nakvaksin.service.exception;

public class TokenHistoryNotFoundException extends RuntimeException {

    public TokenHistoryNotFoundException(String msg) {
        super(msg);
    }
}
