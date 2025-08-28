package com.example.inshorts.exception;

/**
 * Custom exception for LLM service related errors
 */
public class LLMServiceException extends RuntimeException {
    
    private final String errorCode;
    
    public LLMServiceException(String message) {
        super(message);
        this.errorCode = "LLM_SERVICE_ERROR";
    }
    
    public LLMServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public LLMServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "LLM_SERVICE_ERROR";
    }
    
    public LLMServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
