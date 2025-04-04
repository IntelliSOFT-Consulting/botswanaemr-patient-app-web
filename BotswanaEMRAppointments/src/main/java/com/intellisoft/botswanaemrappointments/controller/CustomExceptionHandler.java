package com.intellisoft.botswanaemrappointments.controller;


import com.intellisoft.botswanaemrappointments.service.models.response.ErrorResponse;
import com.intellisoft.botswanaemrappointments.utils.exception.CustomTimeoutException;
import com.intellisoft.botswanaemrappointments.utils.exception.GenericBadRequestException;
import com.intellisoft.botswanaemrappointments.utils.exception.ServerException;
import com.intellisoft.botswanaemrappointments.utils.exception.ServiceException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j2
@ControllerAdvice
public class CustomExceptionHandler {


    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleServiceException(
            ServiceException exception,
            WebRequest request
    ){
        log.error("Object not found.", exception);
        return buildErrorResponse(exception, exception.getMessage(), HttpStatus.BAD_GATEWAY, request);
    }

    @ExceptionHandler(CustomTimeoutException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleCustomTimeoutException(
            CustomTimeoutException exception,
            WebRequest request
    ){
        log.error("Timeout occurred ", exception);
        return buildErrorResponse(exception, exception.getMessage(), HttpStatus.GATEWAY_TIMEOUT, request);
    }
    @ExceptionHandler(ServerException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ErrorResponse> handleServerException(
            ServerException exception,
            WebRequest request
    ){
        log.error("Timeout occurred ", exception);
        return buildErrorResponse(exception, exception.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, request);
    }

    @ExceptionHandler(GenericBadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleGenericBadRequest(
            GenericBadRequestException exception,
            WebRequest request
    ){
        log.error("Bad Request.", exception);
        return buildErrorResponse(exception, exception.getMessage(), HttpStatus.BAD_REQUEST, request);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        var response = buildErrorResponse(ex,"Field validation failed", HttpStatus.BAD_REQUEST,request);

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Objects.requireNonNull(response.getBody()).addValidationError(fieldName,errorMessage);
        });
        return response;
    }


        private ResponseEntity<ErrorResponse> buildErrorResponse(
                Exception exception,
                String message,
                HttpStatus httpStatus,
                WebRequest request
        ) {
            ErrorResponse errorResponse = new ErrorResponse(
                    httpStatus.value(),
                    message
            );

            return ResponseEntity.status(httpStatus).body(errorResponse);
        }


}
