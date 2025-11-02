package com.snow.util;

import com.snow.exceptions.ForbiddenRequestException;
import com.snow.exceptions.UnauthorizedRequestException;
import com.snow.http.models.HttpRequest;
import com.snow.http.models.HttpResponse;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class HttpResponseUtil {

    private static final Logger logger = Logger.getLogger(HttpResponseUtil.class.getName());

    public static CompletableFuture<Void> sendNotFound(HttpRequest request, HttpResponse response, Throwable e) {
        logger.severe(String.format("Route %s %s not found", request.method(), request.route()));
        response.status(404);
        response.nativeWrite("Not Found".getBytes());
        return HttpUtil.returnExceptionally(e);
    }

    public static CompletableFuture<Void> sendForbidden(HttpRequest request, HttpResponse response, String role) {
        logger.severe(
                String.format(
                        "Forbidden Request: %s %s - does not have role: %s",
                        request.method(),
                        request.route(),
                        role)
        );
        response.status(403);
        response.nativeWrite("Forbidden".getBytes());
        request.setAttribute("Authorized", false);
        return HttpUtil.returnExceptionally(
                new ForbiddenRequestException(request.method(), request.route(), role)
        );
    }

    public static CompletableFuture<Void> sendUnauthorized(HttpRequest request, HttpResponse response) {
        logger.severe(String.format("Unauthorized Request: %s %s", request.method(), request.route()));
        request.setAttribute("Authenticated", false);
        response.status(401);
        response.nativeWrite("Unauthorized".getBytes());
        return HttpUtil.returnExceptionally(
                new UnauthorizedRequestException(request.method(), request.route())
        );
    }


}
