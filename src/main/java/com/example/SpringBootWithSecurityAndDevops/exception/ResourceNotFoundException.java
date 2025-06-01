package com.example.SpringBootWithSecurityAndDevops.exception;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
