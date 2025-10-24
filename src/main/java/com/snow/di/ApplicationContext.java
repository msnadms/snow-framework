package com.snow.di;

import com.snow.annotations.Inject;
import com.snow.exceptions.ComponentNotFoundException;
import com.snow.exceptions.CyclicDependencyException;
import com.snow.exceptions.DependencyInstantiationException;
import com.snow.util.Lifetime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ApplicationContext {

    private static final Map<Class<?>, Object> singletons = new HashMap<>();
    private static final Map<Class<?>, Object> scoped = new HashMap<>();
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
        if (isCyclic(clazz, new HashSet<>())) {
            throw new CyclicDependencyException(clazz);
        }
        return createComponentHelper(clazz);
    }

    private <T> Object createComponentHelper(Class<T> clazz) {

        if (singletons.containsKey(clazz)) {
            return singletons.get(clazz);
        } else if (scoped.containsKey(clazz)) {
            return scoped.get(clazz);
        }

        var component = components.get(clazz);
        if (component == null) {
            throw new ComponentNotFoundException(clazz);
        }

        Constructor<?> constructor = chooseConstructor(clazz);

        Set<Object> dependencies = new HashSet<>();
        for (var parameter : constructor.getParameters()) {
            dependencies.add(createComponent(parameter.getType()));
        }

        try {
            var instance = constructor.newInstance(dependencies.toArray());
            if (component.getLifetime() == Lifetime.SINGLETON) {
                singletons.put(clazz, instance);
            } else if (component.getLifetime() == Lifetime.SCOPED) {
                scoped.put(clazz, instance);
            }
            return instance;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new DependencyInstantiationException(clazz, e);
        }
    }

    public void clearScopedCache() {
        scoped.clear();
    }

    private boolean isCyclic(Class<?> clazz, Set<Class<?>> path) {
        if (path.contains(clazz)) {
            return true;
        }
        path.add(clazz);
        for (var node : chooseConstructor(clazz).getParameters()) {
            if (isCyclic(node.getType(), path)) {
                return true;
            }
        }

        path.remove(clazz);
        return false;
    }

    private Constructor<?> chooseConstructor(Class<?> clazz) {
        return Arrays.stream(clazz.getConstructors())
                    .filter(c -> c.isAnnotationPresent(Inject.class))
                    .findFirst()
                    .orElseGet(() -> {
                        try {
                            return clazz.getConstructor();
                        } catch (NoSuchMethodException e) {
                            throw new DependencyInstantiationException(clazz, e);
                        }
                    });
    }
}
