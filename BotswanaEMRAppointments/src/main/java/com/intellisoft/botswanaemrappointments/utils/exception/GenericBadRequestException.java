package com.intellisoft.botswanaemrappointments.utils.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GenericBadRequestException extends ResponseStatusException {

    public GenericBadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST,message);
    }
}
