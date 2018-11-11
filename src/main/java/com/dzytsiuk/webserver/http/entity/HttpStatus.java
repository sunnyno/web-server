package com.dzytsiuk.webserver.http.entity;

public class HttpStatus {
    private final String code;
    private String message;

    public HttpStatus(Integer code, String message) {
        this.code = String.valueOf(code);
        this.message = message;
    }

    public HttpStatus(StandardHttpStatus standardHttpStatus) {
        this.code = String.valueOf(standardHttpStatus.getCode());
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
