package com.dzytsiuk.webserver.http.entity;

public enum HttpMethod {
    GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE");
    private String methodName;

    HttpMethod(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public static HttpMethod getMethodByName(String name) {
        for (HttpMethod httpMethod : HttpMethod.values()) {
            if (name.equalsIgnoreCase(httpMethod.getMethodName())) {
                return httpMethod;
            }
        }
        return null;
    }
}
