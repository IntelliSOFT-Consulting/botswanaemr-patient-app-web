package com.intellisoft.botswanaemrauthentication.consent.service;

import com.intellisoft.botswanaemrauthentication.DbConsent;
import com.intellisoft.botswanaemrauthentication.Results;

public interface ConsentService {

    //Add Consent
    Results addConsent(DbConsent dbConsent, String userId);

    //Get Consent details
    Results getConsent(String consentId);

    //Update Consent for a particular user
    Results updateConsent(String consentId, DbConsent consent, String userId);

    //get all consents for a particular user
    Results getUserConsents(String userId);

    //Delete Consent for a particular user
    Results deleteConsentByUserId(String userId, String consentId);


}
