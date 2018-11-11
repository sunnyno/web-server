package com.dzytsiuk.webserver.http.entity;

public final class HttpHeader {
    private final String name;
    private final String value;


    public HttpHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
