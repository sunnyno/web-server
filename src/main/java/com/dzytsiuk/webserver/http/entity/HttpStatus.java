package com.dzytsiuk.webserver.http.entity;

public class HttpStatus {
    private int code;
    private String message;

    public HttpStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public HttpStatus(StandardHttpStatus standardHttpStatus) {
        this.code = standardHttpStatus.getCode();
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
