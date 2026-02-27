package com.intellisoft.botswanaemrauthentication.authentication.registration;

/**
 * Thrown when a downstream OpenMRS REST call returns a non-2xx response
 * or fails with a network/timeout error.
 */
public class OpenMrsCallException extends RuntimeException {

    private final int statusCode;
    private final String resource;

    public OpenMrsCallException(String resource, int statusCode, String message) {
        super(String.format("OpenMRS call failed [resource=%s, status=%d]: %s",
                resource, statusCode, message));
        this.statusCode = statusCode;
        this.resource   = resource;
    }

    public OpenMrsCallException(String resource, Throwable cause) {
        super(String.format("OpenMRS call failed [resource=%s]: %s", resource, cause.getMessage()), cause);
        this.statusCode = -1;
        this.resource   = resource;
    }

    public int getStatusCode() { return statusCode; }
    public String getResource() { return resource; }
}
