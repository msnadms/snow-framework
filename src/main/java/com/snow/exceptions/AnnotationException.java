package com.snow.exceptions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationException extends RuntimeException {
    public AnnotationException(Annotation annotation, Method method, Throwable cause) {
        super(
                "Error with annotation: " + annotation.annotationType().getName() + " on method: " + method.getName(),
                cause
        );
    }
    public AnnotationException(Method method) {
        super("Error with annotations on method: " + method.getName());
    }
}
