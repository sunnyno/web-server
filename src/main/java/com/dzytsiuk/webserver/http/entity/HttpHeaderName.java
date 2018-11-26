package com.dzytsiuk.webserver.http.entity;

public enum HttpHeaderName {
    CONTENT_LENGTH("Content-Length"), CONTENT_TYPE("Content-Type"), TRANSFER_ENCODING_CHUNKED("Transfer-Encoding: chunked"),
    HOST("Host"), CONNECTION("Connection"), CACHE_CONTROL("Cache-Control"), USER_AGENT("User-Agent"), ACCEPT("Accept"),
    COOKIE("Cookie"), SET_COOKIE("Set-Cookie"), DATE("Date"), LOCATION("Location"), ACCEPT_LANGUAGE("Accept-Language");
    private String headerName;

    HttpHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public static HttpHeaderName getHeaderByHeaderName(String headerName) {
        for (HttpHeaderName httpHeaderName : HttpHeaderName.values()) {
            if (headerName.equalsIgnoreCase(httpHeaderName.getHeaderName())) {
                return httpHeaderName;
            }
        }
        return null;
    }

    public String getHeaderName() {
        return headerName;
    }
}
