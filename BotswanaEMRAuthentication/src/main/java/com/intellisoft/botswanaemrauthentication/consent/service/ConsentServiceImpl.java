package com.intellisoft.botswanaemrauthentication.consent.service;

import com.intellisoft.botswanaemrauthentication.DbConsent;
import com.intellisoft.botswanaemrauthentication.DbNotification;
import com.intellisoft.botswanaemrauthentication.NotificationDetails;
import com.intellisoft.botswanaemrauthentication.Results;
import com.intellisoft.botswanaemrauthentication.authentication.service_class.PatientDetailsServiceImpl;
import com.intellisoft.botswanaemrauthentication.consent.repository.ConsentRepository;
import com.intellisoft.botswanaemrauthentication.consent.entity.Consent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsentServiceImpl implements ConsentService {

    @Autowired
    private ConsentRepository consentRepository;

    @Autowired
    private PatientDetailsServiceImpl patientDetailsService;

    @Override
    public Results addConsent(DbConsent dbConsent, String userId) {

        String language = dbConsent.getLanguage();
        String plannedOperation = dbConsent.getPlannedOperation();
        String patientName = dbConsent.getPatientName();
        String operationNature = dbConsent.getOperationNature();
        String patientRelationship = dbConsent.getPatientRelationship();
        String signedBy = dbConsent.getSignedBy();
        String witnessBy = dbConsent.getWitnessBy();

        Consent consent = new Consent(language, plannedOperation, patientName,
                operationNature, patientRelationship, signedBy, witnessBy, userId);
        consentRepository.save(consent);

        //Create a notification about the consent

        String notification = "There was a new consent added by "
                + signedBy + " for " + patientName + " for " + plannedOperation +
                " on " + consent.getCreatedAt();

        DbNotification dbNotification = new DbNotification(
                "Consent Added",
                notification,
                userId,
                NotificationDetails.SYSTEM.name(),
                NotificationDetails.CONSENT.name());
        patientDetailsService.createNotification(dbNotification);

        return new Results(201, "Consent saved successfully");


    }

    @Override
    public Results getConsent(String consentId) {

        Consent consent = getConsetById(consentId);
        Results results;
        if (consent != null){
            results = new Results(200, consent);
        }else {
            results = new Results(400, "We could not find the consent");
        }
        return results;
    }

    private Consent getConsetById(String consentId){
        return consentRepository.findById(consentId).orElse(null);
    }

    @Override
    public Results updateConsent(String consentId, DbConsent dbConsent, String userId) {


        Consent consent = consentRepository.findById(consentId).orElse(null);
        Results results;
        if (consent != null) {

            Consent updateConsent =  consentRepository.findById(consent.getId())
                    .map(consentOld ->{

                        consentOld.setLanguage(dbConsent.getLanguage());
                        consentOld.setPlannedOperation(dbConsent.getPlannedOperation());
                        consentOld.setPatientName(dbConsent.getPatientName());
                        consentOld.setOperationNature(dbConsent.getOperationNature());
                        consentOld.setPatientRelationship(dbConsent.getPatientRelationship());
                        consentOld.setSignedBy(dbConsent.getSignedBy());
                        consentOld.setWitnessBy(dbConsent.getWitnessBy());

                        return consentRepository.save(consentOld);

                    }).orElse(null);

            if (updateConsent != null) {
                results = new Results(200, "Consent details were updated successfully");
            }else {
                results = new Results(400, "We could not update the consent");
            }

        }else {
            results = new Results(400, "We could not find the consent");
        }

        return results;
    }


    @Override
    public Results getUserConsents(String userId) {

        List<Consent> userConsents = consentRepository.findByUserId(userId);
        return new Results(200, userConsents);
    }

    @Override
    public Results deleteConsentByUserId(String userId, String consentId) {

        Consent consent = getConsetById(consentId);
        if (consent != null){
            if (consent.getUserId().equals(userId)){
                consentRepository.delete(consent);
                return new Results(200, "Consent deleted successfully");
            }else {
                return new Results(400, "You are not authorized to delete this consent");
            }
        }else {
            return new Results(400, "We could not find the consent");
        }
    }

}
