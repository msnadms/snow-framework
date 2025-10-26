package com.snow.http;

import java.util.List;

public record ControllerContext(ControllerDefinition definition, List<String> routeParameters) {}
