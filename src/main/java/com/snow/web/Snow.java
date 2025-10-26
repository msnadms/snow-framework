package com.snow.web;

import com.snow.di.ApplicationContext;
import com.snow.http.models.HttpHandler;

public class Snow {

    private final ApplicationContext context;

    public Snow(String baseUrl) {
        this.context = ApplicationContext.get(baseUrl);
    }

    public HttpHandler receive() {
        return (request, response) -> {
            DispatcherService dispatcherService = new DispatcherService(context, request);

        };
    }



}
