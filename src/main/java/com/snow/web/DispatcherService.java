package com.snow.web;

import com.snow.annotations.params.FromQuery;
import com.snow.annotations.params.FromRoute;
import com.snow.di.ComponentFactory;
import com.snow.exceptions.BadRouteException;
import com.snow.http.*;
import com.snow.http.models.HttpRequest;
import com.snow.util.HttpUtil;
import com.snow.util.ObjectConverter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

public class DispatcherService {

    private final ComponentFactory context;
    private final ControllerDefinition controllerDefinition;
    private final List<String> routeParameters;

    private final HttpRequest request;

    public DispatcherService(ComponentFactory context, HttpRequest request) throws BadRouteException {
        this.context = context;
        var controllerContext = RoutingHelper.getControllerContext(request.method(), request.route());
        this.routeParameters = controllerContext.routeParameters();
        this.controllerDefinition = controllerContext.definition();
        this.request = request;
    }

    public Object invokeControllerMethod() {
        var method = controllerDefinition.method();
        var controllerRoute = HttpUtil.getMapping(method, "").route();
        try {
            var controller = context.createComponent(controllerDefinition.clazz());
            var controllerParameters = controllerDefinition.parameters();
            return method.invoke(
                    controller,
                    parseParameters(controllerRoute, controllerParameters)
            );
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] parseParameters(String controllerRoute, List<ControllerParameter> controllerParameters) {
        Object[] parameters = new Object[controllerParameters.size()];

        Map<String, String> queryParams = HttpUtil.getQueryParams(this.request.route());
        Map<String, String> routeParams = HttpUtil.getRouteParams(controllerRoute, this.routeParameters);

        for (int i = 0; i < controllerParameters.size(); i++) {
            var controllerParameter = controllerParameters.get(i);
            parameters[i] = switch (controllerParameter.source()) {
                case QUERY -> parseQueryParam(queryParams, controllerParameter.parameter());
                case ROUTE -> parseRouteParam(routeParams, controllerParameter.parameter());
                case BODY -> parseBodyParam(controllerParameter.parameter());
            };
        }
        return parameters;
    }

    private Object parseQueryParam(Map<String, String> queryParams, Parameter parameter) {
        Class<?> type = parameter.getType();
        var paramAsString = queryParams.get(getParamName(parameter, true));
        return ObjectConverter.convert(paramAsString, type);
    }

    private Object parseRouteParam(Map<String, String> routeMap, Parameter parameter) {
        Class<?> type = parameter.getType();
        var paramAsString = routeMap.get(getParamName(parameter, false));
        return ObjectConverter.convert(paramAsString, type);
    }

    private Object parseBodyParam(Parameter parameter) {
        return null;
    }

    private String getParamName(Parameter parameter, boolean query) {
        String override = query
                ? parameter.getAnnotation(FromQuery.class).value()
                : parameter.getAnnotation(FromRoute.class).value();
        return override.isEmpty() && parameter.isNamePresent() ? parameter.getName() : override;
    }
}
