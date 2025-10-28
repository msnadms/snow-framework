package com.snow.middleware.functions;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface MiddlewareChain {
    CompletableFuture<Void> execAsync();
}
