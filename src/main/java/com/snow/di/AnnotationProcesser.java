package com.snow.di;

import com.snow.annotations.Controller;
import com.snow.annotations.Component;
import com.snow.util.Lifetime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnnotationProcesser {

    private final Set<Class<?>> controllers;
    private final Map<Class<?>, ComponentDefinition> components;

    private static AnnotationProcesser instance;

    public static AnnotationProcesser get(String basePath) {
        if (instance == null) {
            instance = new AnnotationProcesser(basePath);
        }
        return instance;
    }

    private AnnotationProcesser(String basePath) {
        controllers = new HashSet<>();
        components = new HashMap<>();
        var classes = ComponentScanner.scan(basePath);
        for (var clazz : classes) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                controllers.add(clazz);
            }
            if (clazz.isAnnotationPresent(Component.class)) {
                components.put(
                        clazz,
                        new ComponentDefinition(
                                clazz,
                                clazz.getAnnotation(Component.class).value() == Lifetime.SINGLETON
                        )
                );
            }
        }
    }

    public Set<Class<?>> getControllers() {
        return controllers;
    }

    public Map<Class<?>, ComponentDefinition> getComponents() {
        return components;
    }

}
