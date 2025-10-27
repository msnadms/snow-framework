package com.snow.exceptions;

public class ExistingRouteException extends RuntimeException {
    public ExistingRouteException(String method, String route) {
        super("Route " + method + " " + route + " already exists");
    }
}
