package com.snow.exceptions;

public class ParameterException extends RuntimeException {

    public ParameterException(int annotatedNum, int paramNum, String methodName) {
        super(
            String.format(
                "Controller method %s has %d parameters, however only %d are annotated correctly",
                methodName, paramNum, annotatedNum
            )
        );
    }

    public ParameterException(String controllerParam) {
        super(String.format("Controller param %s is not a parameter in the method", controllerParam));
    }
}
