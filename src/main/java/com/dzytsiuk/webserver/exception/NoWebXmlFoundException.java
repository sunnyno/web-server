package com.dzytsiuk.webserver.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoWebXmlFoundException extends RuntimeException {
    private final Logger log = LoggerFactory.getLogger(getClass());
    public NoWebXmlFoundException(String message) {
        log.error(message);
    }
}
