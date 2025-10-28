package com.snow.middleware;

import com.snow.http.models.HttpRequest;
import com.snow.http.models.HttpResponse;
import com.snow.middleware.functions.MiddlewareChain;
import com.snow.middleware.functions.MiddlewareFn;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class RequestInfoLogging implements MiddlewareFn {

    private final Logger logger = Logger.getLogger(RequestInfoLogging.class.getName());

    @Override
    public CompletableFuture<Void> exec(HttpRequest request, HttpResponse response, MiddlewareChain next) {
        String requestInfo = String.format("%s %s", request.method(), request.route());
        long start = System.nanoTime();
        logger.info(String.format("Received new request: %s", requestInfo));
        return next.execAsync().thenRun(() -> {
            long timeTaken = System.nanoTime() - start;
            logger.info(String.format(
                    "Request %s completed with status: %d in %d ms",
                    requestInfo,
                    response.getStatus(),
                    TimeUnit.NANOSECONDS.toMillis(timeTaken)
            ));
        });
    }
}
