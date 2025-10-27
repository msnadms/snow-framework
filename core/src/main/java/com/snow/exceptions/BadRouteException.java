package com.snow.exceptions;

public class BadRouteException extends Exception {
    public BadRouteException(String method, String route) {
        super("Route " + method + " " + route + " does not exist");
    }
}
