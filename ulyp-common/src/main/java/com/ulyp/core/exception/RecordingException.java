package com.ulyp.core.exception;

/**
 * Indicates that something wrong happened during recording of the object
 */
public class RecordingException extends UlypException {

    public RecordingException(String message, Throwable cause) {
        super(message, cause);
    }
}
