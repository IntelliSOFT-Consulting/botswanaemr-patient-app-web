package com.intellisoft.botswanaemrauthentication.authentication.controller;

import com.intellisoft.botswanaemrauthentication.*;

import com.intellisoft.botswanaemrauthentication.authentication.service_class.PatientDetailsService;
import com.intellisoft.botswanaemrauthentication.authentication.service_class.PatientDetailsServiceImpl;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@RequestMapping(value = "/auths/api/v1")
@RestController
public class AuthenticationController {


    private final PatientDetailsService patientDetailsService;


    FormatterClass formatterClass = new FormatterClass();

    public AuthenticationController(PatientDetailsServiceImpl patientDetailsService) {
        this.patientDetailsService = patientDetailsService;
    }

    //Register patient
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest registerRequest) {

        try {

            Results results = patientDetailsService.addPatient(registerRequest);
            return formatterClass.getResponse(results);

        }catch (Exception e){

            e.printStackTrace();

            Results results = new Results(400, "There was something wrong with the request. Try after some time");
            return ResponseEntity.badRequest().body(results);
        }



    }

    //Login patient
    @PostMapping(path = "/login")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        Results results = patientDetailsService.loginUser(loginRequest);
        return formatterClass.getResponse(results);

    }


    //Refresh token
    @PostMapping(path = "/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody DbRefreshToken refreshTokenRequest)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        Results results = patientDetailsService.refreshToken(refreshTokenRequest);
        return formatterClass.getResponse(results);

    }

    //Resend verification link
    @PostMapping(path = "/resend-link")
    public ResponseEntity<?> resendLink(@RequestBody ResendLink resendLink){

        Results results = patientDetailsService.resendLink(resendLink);
        return formatterClass.getResponse(results);

    }

    //Verify link
    @GetMapping(value = "/verify-link")
    public String getVerificationDetails(
            @Param("link") String link,
            @Param("req_id") String req_id
    ){

        Results results = patientDetailsService.verifyLink(link, req_id);
        return (String) results.getMessage();

    }

    //Request reset password
    @PostMapping(path = "/request-password-reset")
    public ResponseEntity<?> requestResetPassword(@RequestBody ResendLink resendLink){

        Results results = patientDetailsService.requestResetPassword(resendLink);
        return formatterClass.getResponse(results);

    }

    //Reset password
    @PostMapping(path = "/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        Results results = patientDetailsService.resetPassword(resetPasswordRequest);
        return formatterClass.getResponse(results);

    }

    //Link patient
    @PostMapping(path = "/link-patient")
    public ResponseEntity<?> linkPatient(@RequestBody LinkPatientRequest linkPatientRequest) {
        try {
            Results results = patientDetailsService.linkPatient(linkPatientRequest);
            return formatterClass.getResponse(results);
        } catch (Exception e) {
            e.printStackTrace();
            Results results = new Results(400, "There was something wrong with the request. Try after some time");
            return ResponseEntity.badRequest().body(results);
        }
    }

    //Unlink patient
    @PostMapping(path = "/unlink-patient")
    public ResponseEntity<?> unlinkPatient(@RequestBody LinkPatientRequest linkPatientRequest) {
        try {
            Results results = patientDetailsService.unlinkPatient(linkPatientRequest);
            return formatterClass.getResponse(results);
        } catch (Exception e) {
            e.printStackTrace();
            Results results = new Results(400, "There was something wrong with the request. Try after some time");
            return ResponseEntity.badRequest().body(results);
        }
    }

}
