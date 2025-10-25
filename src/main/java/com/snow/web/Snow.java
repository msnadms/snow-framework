package com.snow.web;

import com.snow.di.ApplicationContext;
import com.snow.http.HttpHandler;
import com.snow.http.HttpUtil;

public class Snow {

    private final ApplicationContext context;

    public Snow(String baseUrl) {
        this.context = ApplicationContext.get(baseUrl);
    }

    public HttpHandler receive() {
        return (request, response) -> {
            String route = HttpUtil.getRoutingKey(request.method(), HttpUtil.getSimpleRoute(request.route()));
            DispatcherService dispatcherService = new DispatcherService(context, route);

        };
    }



}
