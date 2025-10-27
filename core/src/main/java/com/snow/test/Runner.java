package com.snow.test;

import com.snow.di.ComponentFactory;
import com.snow.exceptions.BadRequestException;
import com.snow.exceptions.BadRouteException;
import com.snow.http.models.HttpRequest;
import com.snow.web.DispatcherService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Runner {

    public static void main(String[] args) throws BadRouteException, BadRequestException {
        var context = ComponentFactory.get("com.snow");
        context.createComponent(TestObjOne.class);
        DispatcherService service = new DispatcherService(context, getRequest("GET", "users/123?one=test"));
        var result = service.invokeControllerMethod();
        System.out.println(result);
        service = new DispatcherService(context, getRequest("POST", "users/settings/"));
        result = service.invokeControllerMethod();
        System.out.println(result);
        service = new DispatcherService(context, getRequest("POST", "users"));
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
            public Map<String, List<String>> headers() {
                return Map.of("Content-Length", List.of("1"), "Content-Type", List.of("application/json"));
            }

            @Override
            public InputStream body() {
                String json = "{\"id\": 1, \"firstName\": \"mason\", \"lastName\": \"adams\"}";
                return new ByteArrayInputStream(json.getBytes());
            }
        };
    }
}
