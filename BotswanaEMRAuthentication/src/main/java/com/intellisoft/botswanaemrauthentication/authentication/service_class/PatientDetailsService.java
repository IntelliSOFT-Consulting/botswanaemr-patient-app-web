package com.intellisoft.botswanaemrauthentication.authentication.service_class;

import com.intellisoft.botswanaemrauthentication.*;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public interface PatientDetailsService {

    Results addPatient(RegisterRequest registerRequest) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException;
    Results verifyLink(String verifyLink, String userId);
    Results loginUser(LoginRequest loginRequest) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException;
    Results resendLink(ResendLink resendLink);
    Results requestResetPassword(ResendLink resendLink);
    Results resetPassword(ResetPasswordRequest resetPasswordRequest) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException;
    Results findKeycloakUserId(String keycloakId);
    Results updatePatientInfo(UpdateUserDetails userDetails,  String keycloakId);
    Results getUserDetails(String keycloakId);
    Results searchUser(String emailAddress);
    Results updateUserRole(String keycloakId, String role) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException;

    //Get Conditions
    Results getConditions(String userId);

    //Get Allergy
    Results getAllergy(String userId);

    //Get Drugs
    Results getDrugs(String userId);
    //Get Drugs Details
    Results getDrugsDetails(String userId, String drugId);

    Results refreshToken(DbRefreshToken refreshTokenRequest) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException;

    //Get Medical history
    Results getMedicalHistory(String userId);

}
