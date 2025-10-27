package com.snow.web;

import com.snow.di.ComponentFactory;
import com.snow.exceptions.BadRouteException;
import com.snow.http.models.HttpHandler;

public class Snow {

    private final ComponentFactory context;

    public Snow(String baseUrl) {
        this.context = ComponentFactory.get(baseUrl);
    }

    public HttpHandler receive() {
        return (request, response) -> {
            try {
                DispatcherService dispatcherService = new DispatcherService(context, request);
            } catch (BadRouteException e) {
                //404 here
                throw new RuntimeException(e);
            }

        };
    }



}
