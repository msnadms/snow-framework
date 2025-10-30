package com.snow.http.models;

import com.snow.exceptions.BadRouteException;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface HttpHandler {
    CompletionStage<Void> handle(HttpRequest request, HttpResponse response) throws BadRouteException;
}
