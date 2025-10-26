package com.snow.di;

import com.snow.annotations.Controller;
import com.snow.annotations.Component;
import com.snow.annotations.Route;
import com.snow.annotations.params.FromBody;
import com.snow.annotations.params.FromQuery;
import com.snow.annotations.params.FromRoute;
import com.snow.exceptions.ParameterException;
import com.snow.http.ControllerDefinition;
import com.snow.http.ControllerParameter;
import com.snow.util.HttpUtil;
import com.snow.util.Lifetime;
import com.snow.util.ParameterSource;
import com.snow.web.RoutingHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationProcessor {

    private final Map<Class<?>, Lifetime> components;

    private static AnnotationProcessor instance;

    public static AnnotationProcessor get(String basePath) {
        if (instance == null) {
            instance = new AnnotationProcessor(basePath);
        }
        return instance;
    }

    private AnnotationProcessor(String basePath) {
        components = new HashMap<>();
        var classes = ComponentScanner.scan(basePath);
        for (var clazz : classes) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                setControllerMethod(clazz);
            }
            if (clazz.isAnnotationPresent(Component.class)) {
                components.put(
                        clazz,
                        clazz.getAnnotation(Component.class).value()
                );
            }
        }
    }

    private void setControllerMethod(Class<?> clazz) {
        for (var method : clazz.getMethods()) {
            if (!method.isAnnotationPresent(Route.class)) {
                continue;
            }
            var route = method.getAnnotation(Route.class);

            var controllerDefinition = new ControllerDefinition(clazz, method, getMethodParameters(method));
            RoutingHelper.mapDynamicRoute(
                    route.method(),
                    HttpUtil.getRoutingKey(clazz.getAnnotation(Controller.class).value(), route.path()),
                    controllerDefinition
            );
        }
    }

    private List<ControllerParameter> getMethodParameters(Method method) {
        List<ControllerParameter> parameters = new ArrayList<>();
        var methodParams = method.getParameters();
        for (var parameter : methodParams) {
            if (parameter.isAnnotationPresent(FromQuery.class)) {
                parameters.add(new ControllerParameter(parameter, ParameterSource.QUERY));
            } else if (parameter.isAnnotationPresent(FromRoute.class)) {
                parameters.add(new ControllerParameter(parameter, ParameterSource.ROUTE));
            } else if (parameter.isAnnotationPresent(FromBody.class)) {
                parameters.add(new ControllerParameter(parameter, ParameterSource.BODY));
            }
        }
        if (methodParams.length != parameters.size()) {
            throw new ParameterException(methodParams.length, parameters.size(), method.getName());
        }
        return parameters;
    }

    public Map<Class<?>, Lifetime> getComponents() {
        return components;
    }

}
