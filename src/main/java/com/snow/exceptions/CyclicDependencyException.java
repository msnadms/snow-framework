package com.snow.exceptions;

public class CyclicDependencyException extends RuntimeException {
    public CyclicDependencyException(Class<?> clazz) {
        super("Cyclic dependency detected: " + clazz.getName());
    }
}
