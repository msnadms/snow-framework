package com.snow.web;

import com.snow.exceptions.ExistingRouteException;
import com.snow.http.ControllerDefinition;
import com.snow.http.ControllerContext;
import com.snow.http.models.RouteNode;
import com.snow.util.HttpUtil;

import java.util.ArrayList;
import java.util.List;

public class RoutingHelper {

    private static final RouteNode routeNode = new RouteNode();

    public static synchronized void mapDynamicRoute(String method, String route, ControllerDefinition definition) {
        String[] segments = route.split("/");
        RouteNode node = routeNode;
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            if (segment.charAt(0) == '{' && segment.charAt(segment.length() - 1) == '}') {
                if (node.dynamicChild != null) {
                    node = node.dynamicChild;
                    continue;
                }
                node.dynamicChild = new RouteNode();
                node = node.dynamicChild;
            } else {
                if (node.staticChildren.containsKey(segment)) {
                    node = node.staticChildren.get(segment);
                } else {
                    var child = new RouteNode();
                    node.staticChildren.put(segment, child);
                    node = child;
                }
            }
        }
        if (node.controllers.containsKey(method)) {
            throw new ExistingRouteException(method, route);
        }
        node.controllers.put(method, definition);
    }

    public static ControllerContext getControllerContext(String method, String route) {
        RouteNode node = routeNode;
        String[] segments = HttpUtil.getSimpleRoute(HttpUtil.normalizeRoute(route)).split("/");
        List<String> paramValues = new ArrayList<>();
        for (String segment : segments) {
            if (node.staticChildren.containsKey(segment)) {
                node = node.staticChildren.get(segment);
            } else if (node.dynamicChild != null) {
                node = node.dynamicChild;
                paramValues.add(segment);
            } else {
                throw new IllegalArgumentException("Route " + method + " " + route + " does not exist");
            }
        }
        var definition = node.controllers.get(method);
        if (definition == null) {
            throw new IllegalArgumentException("Route " + method + " " + route + " does not exist");
        }
        return new ControllerContext(definition, paramValues);
    }
}
