package com.sisimpur.library.exception;

public class InvalidUserRoleException extends RuntimeException {
    public InvalidUserRoleException(String message) {
        super(message);
    }
    
    public InvalidUserRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
