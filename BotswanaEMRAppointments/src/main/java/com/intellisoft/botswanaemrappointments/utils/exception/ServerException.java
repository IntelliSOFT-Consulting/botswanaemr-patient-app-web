package com.intellisoft.botswanaemrappointments.utils.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ServerException extends ResponseStatusException {
    public ServerException(String message)
    {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
