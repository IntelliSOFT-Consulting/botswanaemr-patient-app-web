package com.intellisoft.botswanaemrappointments.utils.exception;


public class CustomTimeoutException extends RuntimeException {
    public CustomTimeoutException(String errorMessage) {
        super(errorMessage);
    }
}