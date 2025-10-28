package com.snow.http.models;

import com.snow.exceptions.BadRouteException;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface HttpHandler {
    void handle(HttpRequest request, HttpResponse response) throws BadRouteException;
}
