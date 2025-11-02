package com.snow.web;

import com.snow.annotations.params.FromQuery;
import com.snow.annotations.params.FromRoute;
import com.snow.di.ComponentFactory;
import com.snow.exceptions.BadRequestException;
import com.snow.exceptions.BadRouteException;
import com.snow.http.*;
import com.snow.http.models.HttpRequest;
import com.snow.http.models.HttpResponse;
import com.snow.util.HttpResponseUtil;
import com.snow.util.HttpUtil;
import com.snow.util.JsonUtil;
import com.snow.util.ObjectConverter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class DispatcherService {

    private final ComponentFactory context;
    private final ControllerDefinition controllerDefinition;
    private final List<String> routeParameters;

    private final HttpRequest request;
    private final HttpResponse response;

    public DispatcherService(ComponentFactory context, HttpRequest request, HttpResponse response) throws BadRouteException {
        this.context = context;
        var controllerContext = RoutingHelper.getControllerContext(request.method(), request.route());
        this.routeParameters = controllerContext.routeParameters();
        this.controllerDefinition = controllerContext.definition();
        this.request = request;
        this.response = response;
    }

    public CompletableFuture<?> invokeControllerMethod() throws BadRequestException {
        var method = controllerDefinition.method();
        var controllerRoute = HttpUtil.getMapping(method, "").route();
        try {
            if (controllerDefinition.requiredRoles().length > 0 &&
                    !Boolean.TRUE.equals(request.getAttribute("Authorized"))) {
                return HttpResponseUtil.sendForbidden(
                        request,
                        response,
                        Arrays.toString(controllerDefinition.requiredRoles())
                );
            }
            var controller = context.createComponent(controllerDefinition.clazz());
            var result = method.invoke(
                    controller,
                    parseParameters(controllerRoute, controllerDefinition.parameters())
            );
            context.clearScopedCache();
            if (result instanceof CompletionStage<?> cs) {
                return cs.toCompletableFuture();
            }
            return CompletableFuture.completedFuture(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private Object[] parseParameters(String controllerRoute, List<ControllerParameter> controllerParameters)
            throws BadRequestException {
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

    private Object parseBodyParam(Parameter parameter) throws BadRequestException {
        var contentLengthHeader = this.request.headers().get("Content-Length").get(0);
        var contentType = this.request.headers().get("Content-Type").get(0);
        Class<?> resultType = parameter.getType();
        if (contentLengthHeader != null
                && !contentLengthHeader.equals("0")
                && contentType != null
                && contentType.equals("application/json")
        ) {
            try (InputStream in = this.request.body()) {
                return JsonUtil.deserialize(in, resultType);
            } catch (IOException e) {
                throw new BadRequestException("Error deserializing body");
            }
        }
        return null;
    }

    private String getParamName(Parameter parameter, boolean query) {
        String override = query
                ? parameter.getAnnotation(FromQuery.class).value()
                : parameter.getAnnotation(FromRoute.class).value();
        return override.isEmpty() && parameter.isNamePresent() ? parameter.getName() : override;
    }
}
