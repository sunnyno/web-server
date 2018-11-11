package com.dzytsiuk.webserver.http.entity;

public enum StandardHttpStatus {
    OK(200, "OK"), BAD_REQUEST(400, "Bad Request"), NOT_FOUND(404, "Not Found"), REDIRECT(302, "Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"), INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private final int code;
    private final String status;

    StandardHttpStatus(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public static StandardHttpStatus getStatusByCode(int code) {
        for (StandardHttpStatus standardHttpStatus : StandardHttpStatus.values()) {
            if (code == standardHttpStatus.getCode()) {
                return standardHttpStatus;
            }
        }
        return null;
    }
}
