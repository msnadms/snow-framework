package com.snow.exceptions;

public class UnauthorizedRequestException extends RuntimeException {

    public UnauthorizedRequestException(String method, String route) {
        super(String.format("Unauthorized Request: %s %s", method, route));
    }

}
