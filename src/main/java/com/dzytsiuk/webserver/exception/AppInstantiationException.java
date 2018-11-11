package com.dzytsiuk.webserver.exception;

public class AppInstantiationException extends RuntimeException {

    public AppInstantiationException(String message, Exception cause) {
        super(message, cause);
    }
}
