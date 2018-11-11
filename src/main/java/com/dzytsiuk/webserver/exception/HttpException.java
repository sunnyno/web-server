package com.dzytsiuk.webserver.exception;

import com.dzytsiuk.webserver.http.entity.StandardHttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpException extends RuntimeException {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String message;

    public HttpException(StandardHttpStatus message, Throwable cause) {
        log.error(message.getStatus(), cause);
    }

    public HttpException(String message, Throwable cause) {
        log.error(message, cause);
    }

    public HttpException(String msg) {
        this.message = msg;
        log.error(msg);
    }

    @Override
    public String getMessage() {
        if (this.message != null) {
            return this.message;
        }
        return super.getMessage();
    }
}
