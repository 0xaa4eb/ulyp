package com.ulyp.core.exception;

public class UlypException extends RuntimeException {

    public UlypException(String message) {
        super(message);
    }

    public UlypException(Throwable cause) {
        super(cause);
    }

    public UlypException(String message, Throwable cause) {
        super(message, cause);
    }
}
