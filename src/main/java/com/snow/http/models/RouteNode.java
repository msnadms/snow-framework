package com.snow.http.models;

import com.snow.http.ControllerDefinition;

import java.util.HashMap;
import java.util.Map;

public class RouteNode {

    public Map<String, RouteNode> staticChildren = new HashMap<>();
    public RouteNode dynamicChild = null;
    public Map<String, ControllerDefinition> controllers = new HashMap<>();

}
