package com.snow.web;

import com.snow.di.ComponentFactory;
import com.snow.exceptions.BadRequestException;
import com.snow.exceptions.BadRouteException;
import com.snow.http.models.HttpHandler;
import com.snow.http.models.HttpRequest;
import com.snow.http.models.HttpResponse;
import com.snow.middleware.functions.MiddlewareFn;
import com.snow.util.HttpUtil;
import com.snow.util.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.snow.util.HttpUtil.returnExceptionally;

public class Snow {

    private static final Logger logger = Logger.getLogger(Snow.class.getName());

    private final ComponentFactory context;
    private final List<MiddlewareFn> middlewareFns;

    public Snow(String baseUrl) {
        this.context = ComponentFactory.get(baseUrl);
        this.middlewareFns = new ArrayList<>();
    }

    public CompletableFuture<Void> exec(HttpRequest request, HttpResponse response) {
        return exec(request, response, 0)
                .exceptionally(e -> {
                    handleUncaught(request, response, e);
                    return null;
                });
    }

    public void use(MiddlewareFn middlewareFn) {
        middlewareFns.add(middlewareFn);
    }

    private CompletableFuture<Void> exec(HttpRequest request, HttpResponse response, int idx) {
        try {
            if (idx == middlewareFns.size()) {
                return receive().handle(request, response).toCompletableFuture();
            }
            return middlewareFns.get(idx).exec(
                    request,
                    response,
                    () -> exec(request, response, idx + 1));
        } catch (Exception e) {
            return returnExceptionally(e);
        }
    }

    private HttpHandler receive() {
        return (request, response) -> {
            try {
                DispatcherService dispatcherService = new DispatcherService(context, request, response);
                return dispatcherService.invokeControllerMethod()
                        .thenAccept((result) -> writeResult(request, response, result));
            } catch (BadRequestException | BadRouteException | RuntimeException e) {
                return returnExceptionally(e);
            }
        };
    }

    private void writeResult(HttpRequest request, HttpResponse response, Object result) {
        try {
            byte[] body = JsonUtil.serialize(result).getBytes();
            response.status(HttpUtil.successCode(request.method()));
            response.header("Content-Type", "application/json");
            response.nativeWrite(body);
        } catch (IOException | BadRequestException e) {
            throw new CompletionException(e);
        }
    }

    private void handleUncaught(HttpRequest request, HttpResponse response, Throwable e) {
        if (response.isCommitted()) {
            return;
        }
        Throwable cause = e instanceof CompletionException && e.getCause() != null ? e.getCause() : e;
        logger.log(Level.SEVERE,
                String.format("Unhandled exception processing %s %s", request.method(), request.route()),
                cause);
        int status = cause instanceof Exception ex ? HttpUtil.errorCode(ex) : 500;
        response.status(status);
        response.nativeWrite(statusMessage(status).getBytes());
    }

    private static String statusMessage(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            default -> "Internal Server Error";
        };
    }
}
