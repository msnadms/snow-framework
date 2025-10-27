package com.snow.web;

import com.snow.di.ComponentFactory;
import com.snow.exceptions.BadRequestException;
import com.snow.exceptions.BadRouteException;
import com.snow.http.models.HttpHandler;
import com.snow.util.HttpUtil;
import com.snow.util.JsonUtil;

import java.io.IOException;
import java.io.OutputStream;

@SuppressWarnings("ClassCanBeRecord")
public class Snow {

    private final ComponentFactory context;

    public Snow(String baseUrl) {
        this.context = ComponentFactory.get(baseUrl);
    }

    public HttpHandler receive() {
        return (request, response) -> {
            try {
                DispatcherService dispatcherService = new DispatcherService(context, request);
                var result = dispatcherService.invokeControllerMethod();
                response.status(HttpUtil.successCode(request.method()));
                try (OutputStream out = response.body()) {
                    out.write(JsonUtil.serialize(result).getBytes());
                }
            } catch (BadRouteException | BadRequestException | RuntimeException | IOException e) {
                response.status(HttpUtil.errorCode(e));
            }

        };
    }



}
