package com.snow.di;

import com.snow.annotations.Inject;
import com.snow.exceptions.ComponentNotFoundException;
import com.snow.exceptions.DependencyInstantiationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ApplicationContext {

    private static final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, ComponentDefinition> components;

    private static ApplicationContext instance;

    public static ApplicationContext get(String basePath) {
        if (instance == null) {
            instance = new ApplicationContext(basePath);
        }
        return instance;
    }

    private ApplicationContext(String basePath) {
        this.components = AnnotationProcesser.get(basePath).getComponents();
    }

    public <T> Object createComponent(Class<T> clazz) {

        if (singletons.containsKey(clazz)) {
            return singletons.get(clazz);
        }

        var component = components.get(clazz);
        if (component == null) {
            throw new ComponentNotFoundException(clazz);
        }

        Constructor<?> constructor =
                Arrays.stream(clazz.getConstructors())
                        .filter(c -> c.isAnnotationPresent(Inject.class))
                        .findFirst()
                        .orElseGet(() -> {
                            try {
                                return clazz.getConstructor();
                            } catch (NoSuchMethodException e) {
                                throw new DependencyInstantiationException(clazz, e);
                            }
                        });

        Set<Object> dependencies = new HashSet<>();
        for (var parameter : constructor.getParameters()) {
            dependencies.add(createComponent(parameter.getType()));
        }

        if (component.isSingleton()) {
            singletons.put(clazz, component);
        }

        try {
            return constructor.newInstance(dependencies.toArray());
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new DependencyInstantiationException(clazz, e);
        }
    }
}
