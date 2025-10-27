package com.snow.exceptions;

public class DependencyInstantiationException extends RuntimeException {

    public DependencyInstantiationException(Class<?> clazz, Throwable cause) {
        super("Error while instantiating class: " + clazz.getSimpleName(), cause);
    }

}
