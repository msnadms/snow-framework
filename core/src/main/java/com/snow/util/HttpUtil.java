package com.snow.util;

import com.snow.annotations.methods.HttpMethod;
import com.snow.exceptions.AnnotationException;
import com.snow.exceptions.BadRequestException;
import com.snow.exceptions.BadRouteException;
import com.snow.http.HttpRoutingData;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class HttpUtil {

    private static final Pattern LEADING_SLASH = Pattern.compile("^/+");
    private static final Pattern TRAILING_SLASH = Pattern.compile("/+$");
    private static final Pattern MULTI_SLASH = Pattern.compile("/+");
    private static final Map<Class<? extends Annotation>, HttpMethod> methodMappings = new HashMap<>();
    private static final Map<Class<? extends Exception>, Integer> errorCodeMap =
            Map.of(
                    BadRouteException.class, 404,
                    BadRequestException.class, 400,
                    IOException.class, 400,
                    RuntimeException.class, 500
            );

    public static String getRoutingKey(String controllerRoute, String route) {
        String delimiter = controllerRoute.endsWith("/") || route.startsWith("/") ? "" : "/";
        return controllerRoute.isEmpty() ? route : controllerRoute + delimiter + route;
    }

    public static String getSimpleRoute(String route) {
        int idx = route.indexOf('?');
        return idx == -1 ? route : route.substring(0, idx);
    }

    public static Map<String, String> getQueryParams(String route) {
        int idx = route.indexOf('?');
        if (idx == -1) {
            return Collections.emptyMap();
        }
        String[] params = route.substring(idx + 1).split("&");
        Map<String, String> paramMap = new HashMap<>();
        for (String kvs : params) {
            String[] kv = kvs.split("=");
            paramMap.put(kv[0], kv[1]);
        }
        return paramMap;
    }

    public static Map<String, String> getRouteParams(String controllerRoute, List<String> routeParams) {
        String[] segments = HttpUtil.normalizeRoute(controllerRoute).split("/");
        Map<String, String> paramMap = new HashMap<>();
        int idx = 0;
        for (String segment : segments) {
            if (segment.startsWith("{") && segment.endsWith("}")) {
                paramMap.put(segment.substring(1, segment.length() - 1), routeParams.get(idx++));
            }
        }
        return paramMap;
    }

    public static String normalizeRoute(String route) {
            return MULTI_SLASH.matcher(
                    TRAILING_SLASH.matcher(
                            LEADING_SLASH.matcher(route)
                                    .replaceAll(""))
                            .replaceAll(""))
                    .replaceAll("/");
    }

    public static HttpRoutingData getMapping(Method method, String controllerRoute) {
        for (var annotation : method.getAnnotations()) {
            HttpMethod httpMethod = methodMappings.computeIfAbsent(
                    annotation.annotationType(),
                    a -> a.getAnnotation(HttpMethod.class)
            );
            if (httpMethod != null) {
                try {
                    String methodRoute =
                            (String) annotation.annotationType().getMethod("value").invoke(annotation);
                    String route =
                            HttpUtil.getRoutingKey(controllerRoute, methodRoute);
                    return new HttpRoutingData(
                            httpMethod.method(),
                            route
                    );
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new AnnotationException(annotation, method, e);
                }
            }
        }
        throw new AnnotationException(method);
    }

    public static int errorCode(Exception e) {
        return errorCodeMap.getOrDefault(e.getClass(), 500);
    }

    public static int successCode(String method) throws BadRequestException {
        return switch (method) {
            case "GET", "PUT" -> 200;
            case "POST" -> 201;
            case "DELETE" -> 204;
            default -> throw new BadRequestException("Unrecognized method " + method);
        };
    }
}
