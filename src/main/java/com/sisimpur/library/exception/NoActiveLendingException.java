package com.sisimpur.library.exception;

public class NoActiveLendingException extends RuntimeException {
    public NoActiveLendingException(String message) {
        super(message);
    }
    
    public NoActiveLendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
