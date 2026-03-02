package com.intellisoft.botswanaemrauthentication.authentication.registration;

/**
 * Thrown when a valid patient exists locally but has no OpenMRS UUID,
 * meaning their account has not yet been linked to the EMR system.
 */
public class OpenMrsNotLinkedException extends RuntimeException {

    private final String userId;

    public OpenMrsNotLinkedException(String userId) {
        super("Patient account is not yet linked to OpenMRS for userId: " + userId);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}