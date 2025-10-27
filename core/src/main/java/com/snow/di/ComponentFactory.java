package com.snow.di;

import com.snow.annotations.Inject;
import com.snow.exceptions.ComponentNotFoundException;
import com.snow.exceptions.CyclicDependencyException;
import com.snow.exceptions.DependencyInstantiationException;
import com.snow.util.Lifetime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ComponentFactory {

    private static final Map<Class<?>, Constructor<?>> constructors = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    private final ThreadLocal<Map<Class<?>, Object>> scoped = ThreadLocal.withInitial(HashMap::new);
    private final Map<Class<?>, Lifetime> components;

    private static volatile ComponentFactory instance;

    public synchronized static ComponentFactory get(String basePath) {
        if (instance == null) {
            instance = new ComponentFactory(basePath);
        }
        return instance;
    }

    private ComponentFactory(String basePath) {
        var processor = AnnotationProcessor.get(basePath);
        this.components = Collections.unmodifiableMap(processor.getComponents());
    }

    public <T> T createComponent(Class<T> clazz) {
        if (isCyclic(clazz, new HashSet<>())) {
            throw new CyclicDependencyException(clazz);
        }
        return createComponentHelper(clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> T createComponentHelper(Class<T> clazz) {

        var scopedLocal = scoped.get();
        if (singletons.containsKey(clazz)) {
            return (T) singletons.get(clazz);
        } else if (scopedLocal.containsKey(clazz)) {
            return (T) scopedLocal.get(clazz);
        }

        var lifetime = components.get(clazz);
        if (lifetime == null) {
            throw new ComponentNotFoundException(clazz);
        }

        Constructor<?> constructor = chooseConstructor(clazz);

        List<Object> dependencies = new ArrayList<>();
        for (var parameter : constructor.getParameters()) {
            dependencies.add(createComponentHelper(parameter.getType()));
        }

        Function<? super Class<?>, ?> instanceCreator = (c) -> {
            try {
                return constructor.newInstance(dependencies.toArray());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new DependencyInstantiationException(c, e);
            }
        };

        if (lifetime == Lifetime.SINGLETON) {
            return (T) singletons.computeIfAbsent(clazz, instanceCreator);
        } else if (lifetime == Lifetime.SCOPED) {
            return (T) scopedLocal.computeIfAbsent(clazz, instanceCreator);
        }
        return (T) instanceCreator.apply(clazz);
    }

    public void clearScopedCache() {
        scoped.get().clear();
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
        if (constructors.containsKey(clazz)) {
            return constructors.get(clazz);
        }
        var constructor = Arrays.stream(clazz.getConstructors())
                    .filter(c -> c.isAnnotationPresent(Inject.class))
                    .findFirst()
                    .orElseGet(() -> {
                        try {
                            return clazz.getConstructor();
                        } catch (NoSuchMethodException e) {
                            throw new DependencyInstantiationException(clazz, e);
                        }
                    });
        constructors.put(clazz, constructor);
        return constructor;
    }
}
