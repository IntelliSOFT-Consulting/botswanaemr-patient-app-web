package com.intellisoft.botswanaemrauthentication.authentication.registration;

/**
 * Thrown when a patient record cannot be located in the local database.
 */
public class PatientNotFoundException extends RuntimeException {

    private final String userId;

    public PatientNotFoundException(String userId) {
        super("Patient not found for userId: " + userId);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}

