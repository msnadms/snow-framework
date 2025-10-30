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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.snow.util.HttpUtil.returnExceptionally;

public class Snow {

    private final ComponentFactory context;
    private final List<MiddlewareFn> middlewareFns;

    public Snow(String baseUrl) {
        this.context = ComponentFactory.get(baseUrl);
        this.middlewareFns = new ArrayList<>();
    }

    public void exec(HttpRequest request, HttpResponse response) {
        exec(request, response, 0);
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
                DispatcherService dispatcherService = new DispatcherService(context, request);
                return dispatcherService.invokeControllerMethod().thenAccept((result) -> {
                    try (OutputStream out = response.body()) {
                        out.write(JsonUtil.serialize(result).getBytes());
                        response.status(HttpUtil.successCode(request.method()));
                    } catch (IOException | BadRequestException | RuntimeException e) {
                        response.status(HttpUtil.errorCode(e));
                    }
                });
            } catch (BadRequestException | BadRouteException | RuntimeException e) {
                response.status(HttpUtil.errorCode(e));
                return returnExceptionally(e);
            }
        };
    }
}
