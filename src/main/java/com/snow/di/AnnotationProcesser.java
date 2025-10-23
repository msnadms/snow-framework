package com.snow.di;

import com.snow.annotations.Controller;
import com.snow.annotations.Inject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AnnotationProcesser {

    private final Set<Class<?>> controllers;
    private final Set<Class<?>> serviceClasses;

    private static AnnotationProcesser instance;

    public static AnnotationProcesser get(String basePath) {
        if (instance == null) {
            instance = new AnnotationProcesser(basePath);
            return instance;
        }
        return instance;
    }

    private AnnotationProcesser(String basePath) {
        controllers = new HashSet<>();
        serviceClasses = new HashSet<>();
        var classes = ComponentScanner.scan(basePath);
        for (var clazz : classes) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                controllers.add(clazz);
            }
            if (
                Arrays.stream(clazz.getConstructors())
                        .anyMatch(constructor -> constructor.isAnnotationPresent(Inject.class))
            ) {
                serviceClasses.add(clazz);
            }
        }
    }

    public Set<Class<?>> getControllers() {
        return controllers;
    }

    public Set<Class<?>> getServiceClasses() {
        return serviceClasses;
    }
}
