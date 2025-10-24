package com.snow.exceptions;

public class ComponentNotFoundException extends RuntimeException {

    public ComponentNotFoundException(Class<?> clazz) {
        super(clazz.getName() + " not found or not annotated with @Component");
    }

}
