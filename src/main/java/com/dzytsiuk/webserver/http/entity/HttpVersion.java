package com.dzytsiuk.webserver.http.entity;

public enum HttpVersion {
    HTTP_1_1("HTTP/1.1"), HTTPS_1_1("HTTPS/1.1");
    private String name;

    HttpVersion(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static HttpVersion getVersionByName(String name) {
        for (HttpVersion httpVersion : HttpVersion.values()) {
            if (name.equalsIgnoreCase(httpVersion.getName()))
                return httpVersion;
        }
        return null;
    }
}
