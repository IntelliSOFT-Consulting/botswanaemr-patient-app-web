package com.intellisoft.botswanaemrauthentication.authentication.controller;

import com.intellisoft.botswanaemrauthentication.DbUserRole;
import com.intellisoft.botswanaemrauthentication.FormatterClass;
import com.intellisoft.botswanaemrauthentication.Results;
import com.intellisoft.botswanaemrauthentication.authentication.service_class.PatientDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@RequestMapping(value = "/auths/service/authentication/")
@RestController
public class MicroserviceController {

    @Autowired
    private PatientDetailsServiceImpl patientDetailsService;

    FormatterClass formatterClass = new FormatterClass();

    @GetMapping(value = "user/{keycloakId}")
    public ResponseEntity<?> getUserDetails(
            @PathVariable("keycloakId") String keycloakId){

        Results results = patientDetailsService.findKeycloakUserId(keycloakId);
        return formatterClass.getResponse(results);

    }

    @GetMapping(value = "user/search/{emailAddress}")
    public ResponseEntity<?> searchUser(
            @PathVariable("emailAddress") String emailAddress){

        Results results = patientDetailsService.searchUser(emailAddress);
        return formatterClass.getResponse(results);

    }

    @PostMapping(value = "user/update-role")
    public ResponseEntity<?> updateUser(@RequestBody DbUserRole dbUserRole)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        String keyCloakId = dbUserRole.getKeyCloakId();
        String role = dbUserRole.getRoleName();

        Results results1 = patientDetailsService.updateUserRole(keyCloakId, role);
        return formatterClass.getResponse(results1);

    }


}
