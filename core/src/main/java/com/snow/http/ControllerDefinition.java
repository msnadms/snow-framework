package com.snow.http;

import java.lang.reflect.Method;
import java.util.List;

public record ControllerDefinition(Class<?> clazz, Method method, List<ControllerParameter> parameters) {}
