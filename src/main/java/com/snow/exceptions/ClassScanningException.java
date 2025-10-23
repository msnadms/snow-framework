package com.snow.exceptions;

public class ClassScanningException extends RuntimeException {

    public ClassScanningException(String message) {
        super("Error scanning classes: " + message);
    }

    public ClassScanningException(String path, Throwable cause) {
        super("Failed to load classes from path: " + path, cause);
    }

    public ClassScanningException(Throwable cause) {
        super("Error scanning classes: ", cause);
    }

}
