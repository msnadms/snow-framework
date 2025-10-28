package com.snow.middleware.functions;

import com.snow.http.models.HttpRequest;
import com.snow.http.models.HttpResponse;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface MiddlewareFn {
    CompletableFuture<Void> exec(HttpRequest request, HttpResponse response, MiddlewareChain next);
}
