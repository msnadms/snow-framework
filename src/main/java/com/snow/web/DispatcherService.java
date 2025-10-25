package com.snow.web;

import com.snow.di.ApplicationContext;
import com.snow.http.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

public class DispatcherService {

    private final ControllerDefinition definition;
    private final ApplicationContext context;

    public DispatcherService(ApplicationContext context, String route) {
        this.context = context;
        this.definition = context.getControllerDefinition(route);
    }

    public Object invokeControllerMethod(HttpRequest request, HttpResponse response) {
        var method = definition.method();
        try {
            var controller = context.createComponent(definition.clazz());
            var controllerParameters = definition.parameters();
            return method.invoke(
                    controller,
                    parseParameters(request, controllerParameters)
            );
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] parseParameters(HttpRequest request, List<ControllerParameter> controllerParameters) {
        Object[] parameters = new Object[controllerParameters.size()];
        Map<String, String> queryParams = HttpUtil.getQueryParams(request.route());
        for (int i = 0; i < controllerParameters.size(); i++) {
            var controllerParameter = controllerParameters.get(i);
            parameters[i] = switch (controllerParameter.source()) {
                case QUERY -> parseQueryParam(queryParams, controllerParameter.parameter());
                case ROUTE ->  parseRouteParams(request.route(), controllerParameter.parameter());
                case BODY -> parseBodyParam(request, controllerParameter.parameter());
            };
        }
        return parameters;
    }

    private Object parseQueryParam(Map<String, String> queryParams, Parameter parameter) {
        Class<?> type = parameter.getType();
        var paramAsString = queryParams.get(parameter.getName());

        return null;
    }

    private Object[] parseRouteParams(String route, Parameter parameter) {
        return null;
    }

    private Object parseBodyParam(HttpRequest request, Parameter parameter) {
        return null;
    }
}
