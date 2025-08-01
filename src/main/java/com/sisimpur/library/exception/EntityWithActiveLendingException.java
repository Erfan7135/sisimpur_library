package com.sisimpur.library.exception;

public class EntityWithActiveLendingException extends RuntimeException {
    public EntityWithActiveLendingException(String message) {
        super(message);
    }

    public EntityWithActiveLendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
