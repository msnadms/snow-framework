package com.snow.exceptions;

public class ForbiddenRequestException extends RuntimeException {

    public ForbiddenRequestException(String method, String route, String role) {
        super(String.format("Forbidden Request: %s %s - does not have role: %s", method, route, role));
    }

}
