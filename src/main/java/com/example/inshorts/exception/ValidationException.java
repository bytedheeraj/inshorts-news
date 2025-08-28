package com.example.inshorts.exception;

/**
 * Custom exception for validation related errors
 */
public class ValidationException extends RuntimeException {
    
    private final String fieldName;
    
    public ValidationException(String message) {
        super(message);
        this.fieldName = "GENERAL";
    }
    
    public ValidationException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }
    
    public ValidationException(String message, String fieldName, Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
}
