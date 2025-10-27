package com.snow.http;

import com.snow.util.ParameterSource;

import java.lang.reflect.Parameter;

public record ControllerParameter(Parameter parameter, ParameterSource source) {}
