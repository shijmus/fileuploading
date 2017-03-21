package com.restapi.utils;
/**
 * 
 * Customized storage exception
 */
@SuppressWarnings("serial")
public class StorageNotFoundException extends StorageException {

    public StorageNotFoundException(String message) {
        super(message);
    }

    public StorageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}