package com.ulyp.storage;


import com.ulyp.core.exception.UlypException;

public class StorageException extends UlypException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
