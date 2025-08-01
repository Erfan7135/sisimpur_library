package com.sisimpur.library.exception;

public class BookAlreadyReturnedException extends RuntimeException {
    public BookAlreadyReturnedException(String message) {
        super(message);
    }
    
    public BookAlreadyReturnedException(String message, Throwable cause) {
        super(message, cause);
    }
}
