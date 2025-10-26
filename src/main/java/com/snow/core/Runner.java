package com.snow.core;

import com.snow.di.ComponentFactory;
import com.snow.exceptions.BadRouteException;
import com.snow.http.models.HttpRequest;
import com.snow.test.TestObjOne;
import com.snow.web.DispatcherService;

import java.io.InputStream;
import java.util.Map;

public class Runner {

    public static void main(String[] args) throws BadRouteException {
        var context = ComponentFactory.get("com.snow");
        context.createComponent(TestObjOne.class);
        DispatcherService service = new DispatcherService(context, getRequest("GET", "users/123?one=test"));
        var result = service.invokeControllerMethod();
        System.out.println(result);
        service = new DispatcherService(context, getRequest("POST", "users/settings/"));
        result = service.invokeControllerMethod();
        System.out.println(result);
    }

    private static HttpRequest getRequest(String method, String route) {
        return new HttpRequest() {
            @Override
            public String method() {
                return method;
            }

            @Override
            public String route() {
                return route;
            }

            @Override
            public Map<String, String> headers() {
                return Map.of();
            }

            @Override
            public InputStream body() {
                return null;
            }
        };
    }
}
