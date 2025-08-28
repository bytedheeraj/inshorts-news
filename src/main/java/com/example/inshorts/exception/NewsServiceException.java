package com.example.inshorts.exception;

/**
 * Custom exception for news service related errors
 */
public class NewsServiceException extends RuntimeException {
    
    private final String errorCode;
    
    public NewsServiceException(String message) {
        super(message);
        this.errorCode = "NEWS_SERVICE_ERROR";
    }
    
    public NewsServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public NewsServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "NEWS_SERVICE_ERROR";
    }
    
    public NewsServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
