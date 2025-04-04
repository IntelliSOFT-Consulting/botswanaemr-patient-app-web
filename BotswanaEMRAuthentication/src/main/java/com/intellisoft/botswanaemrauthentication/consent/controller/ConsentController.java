package com.intellisoft.botswanaemrauthentication.consent.controller;


import com.intellisoft.botswanaemrauthentication.DbConsent;
import com.intellisoft.botswanaemrauthentication.FormatterClass;
import com.intellisoft.botswanaemrauthentication.Results;
import com.intellisoft.botswanaemrauthentication.consent.service.ConsentService;
import com.intellisoft.botswanaemrauthentication.consent.service.ConsentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping(value = "/auths/api/v1/consent/")
@RestController
public class ConsentController {

    FormatterClass formatterClass = new FormatterClass();

    @Autowired
    private ConsentService consentService;

    @PostMapping()
    public ResponseEntity<?> createConsent(@RequestBody DbConsent consent) {

        Optional<String> userId = formatterClass.getCurrentUserLogin();

        try {

            Results results;
            if (userId.isPresent()) {
                results = consentService.addConsent(consent, userId.get());
            } else {
                results = new Results(400, "We could not find the user, to add the consent");
            }
            return formatterClass.getResponse(results);

        } catch (Exception e) {
            Results results = new Results(400, "We could not find the user. Please try again after sometime.");
            return formatterClass.getResponse(results);

        }
    }

    @GetMapping(value = "{consentId}")
    public ResponseEntity<?> getConsent(@PathVariable("consentId") String consentId) {

        Optional<String> userId = formatterClass.getCurrentUserLogin();

        try {

            Results results;
            if (userId.isPresent()) {
                results = consentService.getConsent(consentId);
            } else {
                results = new Results(400, "We could not find the user");
            }
            return formatterClass.getResponse(results);

        } catch (Exception e) {
            Results results = new Results(400, "We could not find the user. Please try again after sometime.");
            return formatterClass.getResponse(results);

        }
    }

    @PutMapping(value = "{consentId}")
    public ResponseEntity<?> updateConsent(
            @PathVariable("consentId") String consentId,
            @RequestBody DbConsent consent) {

        Optional<String> userId = formatterClass.getCurrentUserLogin();

        try {

            Results results;
            if (userId.isPresent()) {
                results = consentService.updateConsent(consentId, consent, userId.get());
            } else {
                results = new Results(400, "We could not find the user");
            }
            return formatterClass.getResponse(results);

        } catch (Exception e) {
            Results results = new Results(400, "We could not find the user. Please try again after sometime.");
            return formatterClass.getResponse(results);

        }
    }



    @DeleteMapping(value = "{consentId}")
    public ResponseEntity<?> deleteConsent(@PathVariable("consentId") String consentId) {

        Optional<String> userId = formatterClass.getCurrentUserLogin();

        try {

            Results results;
            if (userId.isPresent()) {
                results = consentService.deleteConsentByUserId(userId.get(), consentId);
            } else {
                results = new Results(400, "We could not find the user");
            }
            return formatterClass.getResponse(results);

        } catch (Exception e) {
            Results results = new Results(400, "We could not find the user. Please try again after sometime.");
            return formatterClass.getResponse(results);

        }
    }

    @GetMapping()
    public ResponseEntity<?> getConsents() {

        Optional<String> userId = formatterClass.getCurrentUserLogin();

        try {

            Results results;
            if (userId.isPresent()) {
                results = consentService.getUserConsents(userId.get());
            } else {
                results = new Results(400, "We could not find the user");
            }
            return formatterClass.getResponse(results);

        } catch (Exception e) {
            Results results = new Results(400, "We could not find the user. Please try again after sometime.");
            return formatterClass.getResponse(results);

        }
    }

}
