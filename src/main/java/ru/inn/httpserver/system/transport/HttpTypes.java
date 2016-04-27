package ru.inn.httpserver.system.transport;


public enum HttpTypes {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS");

    private String method;
    public final static String METHOD_FIELD_TAG = "iAmHttpMethodField";

    private HttpTypes(String method) {
        this.method = method;
    }

    public String getString() {
        return method;
    }
}
