package com.snow.http.models;

@FunctionalInterface
public interface HttpHandler {
    void handle(HttpRequest request, HttpResponse response);
}
