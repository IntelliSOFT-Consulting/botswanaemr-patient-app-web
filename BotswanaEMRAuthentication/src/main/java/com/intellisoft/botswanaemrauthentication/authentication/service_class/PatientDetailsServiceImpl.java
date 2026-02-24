package com.intellisoft.botswanaemrauthentication.authentication.service_class;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellisoft.botswanaemrauthentication.*;
import com.intellisoft.botswanaemrauthentication.authentication.entity.PatientDetails;
import com.intellisoft.botswanaemrauthentication.authentication.repository.PatientDetailsRepository;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.ws.rs.core.Response;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS;
import static org.keycloak.OAuth2Constants.PASSWORD;
import org.apache.commons.codec.binary.Base64;
import org.thymeleaf.TemplateEngine;

@Service
public class PatientDetailsServiceImpl implements PatientDetailsService{

    @Value("${app.keycloak.authServerUrl}")
    private String authServerUrl;
    @Value("${app.keycloak.refresh}")
    private String refreshToken;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${app.keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.resource}")
    private String resource;

    @Value("${app.keycloak.client-secret}")
    private String clientSecret;

    @Value("${app.keycloak.grant-type}")
    private String grant_type;

    @Value("${app.keycloak.realm.username}")
    private String username;
    @Value("${app.keycloak.realm.password}")
    private String password;

    private final NetworkCall networkCall;

    @Autowired
    private PatientDetailsRepository patientDetailsRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    public TemplateEngine templateEngine;

    private FormatterClass formatterClass = new FormatterClass();

    private final RestTemplateConfig restTemplateConfig = new RestTemplateConfig();

    public PatientDetailsServiceImpl(NetworkCall networkCall) {
        this.networkCall = networkCall;
    }

    @Transactional
    @Override
    public Results addPatient(RegisterRequest registerRequest) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {

        String emailAddress = registerRequest.getEmailAddress();
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();

        boolean isPassword = formatterClass.isPasswordMatch(password, confirmPassword);
        boolean isEmailValid = formatterClass.isEmailValid(emailAddress);

        if (isPassword && isEmailValid){
            //Password matches

            boolean isEmailAddress = checkEmailAddress(emailAddress);

            if (!isEmailAddress){

                // Email address does not exist, add user using keycloak
                String userId = createKeycloakUser(registerRequest).getUserId();

                if (!userId.equals("")){

                    String dateOfBirth = registerRequest.getDateOfBirth();
                    String convertedDob = formatterClass.convertDate(dateOfBirth);

                // String openMrsId =

                    // Default patient identification number to empty string if missing
                    String patientIdentificationNo = registerRequest.getPatientIdentificationNo();
                    if (patientIdentificationNo == null) {
                        patientIdentificationNo = "";
                    }

                    //User has been saved in keycloak, save them to our db
                    PatientDetails patientDetails = new PatientDetails(
                            registerRequest.getFirstName(),
                            registerRequest.getLastName(),
                            registerRequest.getPhoneNumber(),
                            convertedDob,
                            registerRequest.getGender(),
                            patientIdentificationNo,
                            registerRequest.getNationalPassportNo(),
                            registerRequest.getIdentificationType(),
                            registerRequest.getEmailAddress(),
                            userId,
                            registerRequest.getUsername(),
                            false,
                            registerRequest.getImageUrl());

                    String notification = "";
                    // Search OpenMRS by national passport number
                    String nationalPassportNo = registerRequest.getNationalPassportNo();
                    if (nationalPassportNo != null && !nationalPassportNo.trim().isEmpty()){

                        //Search for patient in OpenMRS by national passport number
                        Results results = networkCall.searchPatientByNationalPassport(nationalPassportNo);
                        if (results.getCode() == 200){
                            //Patient found in OpenMRS
                            DbOpenMrsPatientSearchResult searchResult = (DbOpenMrsPatientSearchResult) results.getMessage();

                            // Verify phone number matches
                            String openMrsPhoneNumber = searchResult.getPhoneNumber();
                            String providedPhoneNumber = registerRequest.getPhoneNumber();

                            if (openMrsPhoneNumber != null && openMrsPhoneNumber.equals(providedPhoneNumber)){
                                // Phone matches - link patient
                                String openMrsId = searchResult.getOpenMrsId();
                                String openMrsUUId = searchResult.getOpenMrsUuid();

                                patientDetails.setPatientIdentificationNo(openMrsId);
                                patientDetails.setOpenMrsId(openMrsUUId);
                                patientDetails.setPatient(true);

                                notification = notification + "\n" + "Patient linked successfully. You can now access your medical records.";
                            } else {
                                // Phone doesn't match - proceed without linking
                                patientDetails.setPatient(false);
                                notification = notification + "\n" + "Patient linking was not done. The phone number does not match the one in OpenMRS.";
                            }
                        }else {
                            // Patient not found in OpenMRS - proceed without linking
                            patientDetails.setPatient(false);
                            notification = notification + "\n" + "Patient linking was not done. The provided national passport number could not be found in OpenMRS.";
                        }

                    } else {
                        // No national passport number provided
                        patientDetails.setPatient(false);
                    }

                    patientDetails.setId(userId);


                    DbVerificationLink dbVerificationLink = generateVerificationLink(patientDetails);

                    PatientDetails addedPatientDetails = patientDetailsRepository
                            .save(dbVerificationLink.getPatientDetails());

                    formatterClass.sendMail(javaMailSender,
                            templateEngine,
                            addedPatientDetails,
                            dbVerificationLink.getBaseUrl(),
                            dbVerificationLink.getVerificationLink());

                    //Create notification that user has not created a patient

                    DbNotification dbNotification = new DbNotification(
                            "Patient Activation.",
                            "Your account has been created, but you have not activated the patient status." +
                                    notification,
                            addedPatientDetails.getId(),
                            NotificationDetails.SYSTEM.name(),
                            NotificationDetails.PATIENT_ACTIVATION.name());

                    createNotification(dbNotification);

                    return new Results(201,
                            "Patient registration completed successfully.\n" +
                                    "\n" +
                                    "A verification link has been dispatched to the registered email address. Kindly review your inbox (and spam folder) within the next 5 minutes to activate the account and proceed.\n" +
                                    "\n" +
                                    "If the email is not received shortly, please confirm the address provided or request a new verification link.");

                }else {

                    return new Results(400,
                            "There was an issue saving the user please try again after sometime.");

                }

            }else {
                //Email address exists, inform user
                return new Results(400,
                        "The provided email address is already in the system.");
            }
        }else {
            //Password don't match or email is not valid

            String error = "";

            if (!isPassword) error = error + "Your password don't match.";
            if (!isEmailValid) error = error + "Please submit a valid email address.";

            return new Results(400, error);

        }


    }






    @Override
    public Results verifyLink(String verifyLink, String userId) {

        //Get patient with the following user id
        Optional<PatientDetails> optionalPatientDetails = patientDetailsRepository.findById(userId);
        if (optionalPatientDetails.isPresent()){
            //User exists, check if the provided details are valid
            PatientDetails patientDetails = optionalPatientDetails.get();

            boolean isValid = patientDetails.isVerified();
            if (!isValid){
                //User is not verified
                boolean isLinkValid = patientDetails.isOTPValid();
                if (isLinkValid){
                    //Link is valid, proceed with verification

                    String dbVerifyLink = patientDetails.getOneTimeLink();
                    if(dbVerifyLink.equals(verifyLink)){

                        patientDetails.setVerified(true);
                        PatientDetails updatePatientDetails = updatePatientDetails(patientDetails);

                        if (updatePatientDetails != null){

                            //Create Person and Patient in background.

                            String uuid = updatePatientDetails.getId();

                            String givenName = updatePatientDetails.getFirstName();
                            String familyName = updatePatientDetails.getLastName();

                            DbName dbName = new DbName(givenName, familyName);

                            List<DbName> dbNameList = new ArrayList<>();
                            dbNameList.add(dbName);

                            String gender = updatePatientDetails.getGender();
                            String birthDate = updatePatientDetails.getDateOfBirth();

                            DbAddress dbAddress = new DbAddress("Kenya", "Kenya", "Kenya", "00100");
                            List<DbAddress> dbAddressList = new ArrayList<>();

                            dbAddressList.add(dbAddress);

                            DbPerson dbPerson = new DbPerson(dbNameList, gender, birthDate, dbAddressList);

                            formatterClass.createPerson(this, dbPerson,uuid);

                            return new Results(200, "Patient has been verified successfully.");
                        }else {
                            return new Results(400, "Patient could not be verified. Please try again");
                        }

                    }else {
                        return new Results(400, "The provided link is invalid.");
                    }

                }else {
                    return new Results(400, "The link has expired, request a new verification link.");
                }

            }else {
                return new Results(400, "The link has already been used.");
            }

        }else {
            return new Results(400, "The link is not valid please check again before proceeding.");

        }


    }

    @Transactional
    @Override
    public Results loginUser(LoginRequest loginRequest) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        try{

            //Check if user is verified
            String emailAddress = loginRequest.getEmailAddress();
            PatientDetails patientDetails = findByEmailAddress(emailAddress);

            if (patientDetails != null){

                boolean isVerified = patientDetails.isVerified();
                if (isVerified){

                    Map<String, Object> clientCredentials = new HashMap<>();
                    clientCredentials.put("secret", clientSecret);
                    clientCredentials.put("grant_type", "password");

                    HttpClient httpClient = HttpClients
                            .custom()
                            .setSSLContext(
                                    new SSLContextBuilder()
                                            .loadTrustMaterial(null,
                                                    TrustAllStrategy.INSTANCE).build())
                            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                            .build();

                    Configuration configuration =
                            new Configuration(
                                    authServerUrl,
                                    realm,
                                    resource,
                                    clientCredentials,
                                    httpClient);
                    AuthzClient authzClient = AuthzClient.create(configuration);

                    try{

                        AccessTokenResponse response = authzClient
                                .obtainAccessToken(
                                        loginRequest.getEmailAddress(),
                                        loginRequest.getPassword());

                        String accessToken = response.getToken();
                        long expiresIn = response.getExpiresIn();

                        String refreshToken = response.getRefreshToken();
                        long refreshExpiresIn = response.getRefreshExpiresIn();

                        String type = response.getTokenType();

                        List<String> roleList = getRoles(accessToken);


                        LoginResponse loginResponse = new LoginResponse(accessToken, expiresIn,
                                refreshToken, refreshExpiresIn, type, roleList);


                        return new Results(200, loginResponse);

                    }catch (HttpResponseException ex){
                        ex.printStackTrace();
                        return new Results(400, "The provided credentials are invalid.");
                    }


                }else {
                    return new Results(400, "The email address is not verified.");

                }


            }else {
                return new Results(400, "The email address could not be found.");
            }

        }catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e){

            /**TODO: Capture the correct message from keycloak
             *
             */

            System.out.println("---- " + e.getMessage());
            return new Results(400, "Patient could not be logged in.");
        }

    }


    private List<String> getRoles(String jwtToken){

//        System.out.println("------------ Decode JWT ------------");
        String[] split_string = jwtToken.split("\\.");
        String base64EncodedHeader = split_string[0];
        String base64EncodedBody = split_string[1];
        String base64EncodedSignature = split_string[2];

//        System.out.println("~~~~~~~~~ JWT Header ~~~~~~~");
        Base64 base64Url = new Base64(true);
        String header = new String(base64Url.decode(base64EncodedHeader));


        System.out.println("~~~~~~~~~ JWT Body ~~~~~~~");
        String body = new String(base64Url.decode(base64EncodedBody));
        Gson g = new Gson();
        JsonObject jsonObject = g.fromJson(body, JsonObject.class);
        JsonArray roles = jsonObject
                .get("realm_access").getAsJsonObject()
                .get("roles").getAsJsonArray();

        List<String> rolesList = new ArrayList<>();
        for (int i = 0; i < roles.size(); i++){

            String role = roles.get(i).getAsString();
            if (role.equals("offline_access") || role.equals("uma_authorization")) {
            }else {
                rolesList.add(role);
            }

        }
        return rolesList;

    }

    @Transactional
    @Override
    public Results resendLink(ResendLink resendLink) {

        String emailAddress = resendLink.getEmailAddress();

        boolean isEmailAddress = checkEmailAddress(emailAddress);
        if (isEmailAddress){

            PatientDetails patientDetails = findByEmailAddress(emailAddress);
            DbVerificationLink dbVerificationLink = generateVerificationLink(patientDetails);

            PatientDetails updatePatientDetails = updatePatientDetails(dbVerificationLink.getPatientDetails());

            formatterClass.sendMail(javaMailSender,
                    templateEngine,
                    updatePatientDetails,
                    dbVerificationLink.getBaseUrl(),
                    dbVerificationLink.getVerificationLink());


            return new Results(200, "Verification link has been sent to the email address.");


        }else{
            return new Results(400, "The email address could not be found.");
        }

    }

    @Override
    public Results requestResetPassword(ResendLink resendLink) {

        //Request reset password code. Provide code that will be submitted with new password. Send to email address
        String emailAddress = resendLink.getEmailAddress();
        boolean isEmailAddress = checkEmailAddress(emailAddress);

        if (isEmailAddress){
            //Email address exists

            PatientDetails patientDetails = findByEmailAddress(emailAddress);

            DbVerificationLink dbResetOtp = getResetPassword(patientDetails);

            PatientDetails updatePatientDetails = updatePatientDetails(dbResetOtp.getPatientDetails());

            formatterClass.sendResetPasswordMail(javaMailSender,
                    updatePatientDetails,
                    dbResetOtp.getVerificationLink(), templateEngine);


            DbNotification dbNotification = new DbNotification(
                    "Password change",
                    "Someone has requested to change your password.",
                    patientDetails.getId(),
                    NotificationDetails.SYSTEM.name(),
                    NotificationDetails.AUTHENTICATION.name());

            createNotification(dbNotification);

            return new Results(200,
                    new DbResults("A reset password code has been sent to your email address."));

        }else {
            //Email does not exist
            return new Results(400,
                    "The email address could not be found.");
        }

    }

    @Override
    public Results resetPassword(ResetPasswordRequest resetPasswordRequest) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        String emailAddress = resetPasswordRequest.getEmailAddress();
        boolean isEmailAddress = checkEmailAddress(emailAddress);

        String newPassword = resetPasswordRequest.getPassword();
        String confirmPassword = resetPasswordRequest.getConfirmPassword();

        if (newPassword.equals(confirmPassword)){

            if (isEmailAddress){
                //Email address exists
                PatientDetails patientDetails = findByEmailAddress(emailAddress);

                String requestOtpCode = resetPasswordRequest.getOtpCode();
                String dbOtpCode = patientDetails.getOneTimeLink();

                boolean isLinkValid = patientDetails.isOTPValid();

                if (isLinkValid){
                    //Link is valid, proceed with verification

                    if(dbOtpCode.equals(requestOtpCode)){

                        Keycloak keycloak = KeycloakBuilder.builder()
                                .serverUrl(authServerUrl)
                                .realm(realm)
                                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                                .clientId(clientId)
                                .clientSecret(clientSecret)
                                .resteasyClient(
                                        new ResteasyClientBuilder()
                                                .connectionPoolSize(10)
                                                .build())
                                .build();

                        keycloak.tokenManager().getAccessToken();

                        //Change the password
                        String keycloakId = patientDetails.getUserKeycloakId();
                        // create password credential
                        CredentialRepresentation passwordCred = new CredentialRepresentation();
                        passwordCred.setTemporary(false);
                        passwordCred.setType(CredentialRepresentation.PASSWORD);
                        passwordCred.setValue(resetPasswordRequest.getPassword());


                        RealmResource realmResource = keycloak.realm(realm);
                        UsersResource usersRessource = realmResource.users();
                        UserResource userResource = usersRessource.get(keycloakId);
                        userResource.resetPassword(passwordCred);

                        //Create a notification
                        DbNotification dbNotification = new DbNotification(
                                "Password change",
                                "Your password has been changed.",
                                patientDetails.getId(),
                                NotificationDetails.SYSTEM.name(),
                                NotificationDetails.AUTHENTICATION.name());
                        createNotification(dbNotification);

                        return new Results(200, "Password has been changed successfully.");


                    }else {
                        return new Results(400, "The provided otp is invalid.");

                    }

                }else {
                    return new Results(400, "The otp has expired, request a new verification link.");

                }


            }else {
                //Email does not exist
                return new Results(400, "The email address could not be found.");
            }

        }else {
            return new Results(400, "The provided passwords do not match.");

        }




    }

    @Override
    public Results findKeycloakUserId(String keycloakId) {

        PatientDetails patientDetails = getPatientDetails(keycloakId);

        return getPatientResultsData(patientDetails);

    }

    private PatientDetails getPatientData(String id){
        Optional<PatientDetails> optionalPatientDetails = patientDetailsRepository.findById(id);
        return optionalPatientDetails.orElse(null);

    }


    public PatientDetails getPatientDetails(String keycloakId){



        PatientDetails patientDetails = patientDetailsRepository.findByUserKeycloakId(keycloakId);
        if (patientDetails != null){
            return patientDetails;
        }else {
            return null;
        }

    }

    @Override
    public Results updatePatientInfo(UpdateUserDetails userDetails, String keycloakId) {

        String errorDetails = "";
        int code;

        PatientDetails patientDetails = getPatientDetails(keycloakId);
        if (patientDetails != null){

            String firstName = userDetails.getFirstName();
            String lastName = userDetails.getLastName();
            String phoneNumber = userDetails.getPhoneNumber();
            String pin = userDetails.getPatientIdentificationNo();
            String username = userDetails.getUsername();

            String imageUrl = userDetails.getImageUrl();
            String gender = userDetails.getGender();
            String dateOfBirth = userDetails.getDateOfBirth();

            boolean isPatient = patientDetails.isPatient();

            if (firstName != null && !firstName.equals("")) patientDetails.setFirstName(firstName);
            if (lastName != null && !lastName.equals("")) patientDetails.setLastName(lastName);
            if (phoneNumber != null && !phoneNumber.equals("")) patientDetails.setPhoneNumber(phoneNumber);
            if (username != null && !username.equals("")) patientDetails.setUsername(username);

            if (imageUrl != null && !imageUrl.equals("")) patientDetails.setProfileUrl(imageUrl);
            if (gender != null && !gender.equals("")) patientDetails.setGender(gender);
            if (dateOfBirth != null && !dateOfBirth.equals("")) {
                String convertedDob = formatterClass.convertDate(dateOfBirth);
                patientDetails.setDateOfBirth(convertedDob);
            }

            if (pin != null && !pin.equals("")){

                //Check if pin is being used by another patient
                boolean isPinAvailable = patientDetailsRepository.existsByPatientIdentificationNo(pin);
                patientDetails.setPatientIdentificationNo(pin);
                patientDetails.setPatient(true);
                String notification = "Your patient identification number has been updated. " +
                        "You are now a patient.";
                //Get the patient details from openmrs
                Results results = networkCall.getPatientDetails(pin,
                        patientDetails.getFirstName(),
                        patientDetails.getLastName());
                if (results.getCode() == 200){
                    //Patient details found
                    String openMrsUUId = (String) results.getMessage();
                    patientDetails.setOpenMrsId(openMrsUUId);
                    notification = notification + "\n" + " You can now access your medical records.";
                }
                //Create a notification
                DbNotification dbNotification = new DbNotification(
                        "Patient Identification Number",
                        notification,
                        patientDetails.getId(),
                        NotificationDetails.SYSTEM.name(),
                        NotificationDetails.AUTHENTICATION.name());
                createNotification(dbNotification);
            }


//            if (!isPatient){
//
//                if (pin != null && !pin.equals("")){
//
//                    //Check if pin is being used by another patient
//                    boolean isPinAvailable = patientDetailsRepository.existsByPatientIdentificationNo(pin);
//                    if (!isPinAvailable){
//
//                        patientDetails.setPatientIdentificationNo(pin);
//                        patientDetails.setPatient(true);
//                        String notification = "Your patient identification number has been updated. " +
//                                "You are now a patient.";
//                        //Get the patient details from openmrs
//                        Results results = networkCall.getPatientDetails(pin,
//                                patientDetails.getFirstName(),
//                                patientDetails.getLastName());
//                        if (results.getCode() == 200){
//                            //Patient details found
//                            String openMrsUUId = (String) results.getMessage();
//                            patientDetails.setOpenMrsId(openMrsUUId);
//                            notification = notification + "\n" + " You can now access your medical records.";
//                        }
//                        //Create a notification
//                        DbNotification dbNotification = new DbNotification(
//                                "Patient Identification Number",
//                                notification,
//                                patientDetails.getId(),
//                                NotificationDetails.SYSTEM.name(),
//                                NotificationDetails.AUTHENTICATION.name());
//                        createNotification(dbNotification);
//
//                    }else {
//                        errorDetails = errorDetails + "The pin cannot be used because its already in use by another patient.";
//                    }
//                }
//            }else {
//                errorDetails = errorDetails + "The current user is already a patient and cannot be updated to a patient again. ";
//            }

            PatientDetails updatePatientDetails = updateUserDetails(patientDetails);
            if (updatePatientDetails != null){
                code = 200;
                errorDetails = errorDetails  + "User details has been updated successfully.";

            }else {
                code = 400;
                errorDetails = errorDetails + "User could not be updated.";
            }

        }else {
            code = 400;
            errorDetails = "User not found";
        }

        if (code == 200){
            return new Results(code, new DbResults(errorDetails));
        }else {
            return new Results(code, errorDetails);
        }

    }

    @Override
    public Results getUserDetails(String keycloakId) {

        PatientDetails patientDetails = getPatientDetails(keycloakId);
        Results results;

        if (patientDetails != null){

            DbPatientDetails dbPatientDetails = new DbPatientDetails(
                    patientDetails.getId(),
                    patientDetails.getEmailAddress(),
                    patientDetails.getFirstName(),
                    patientDetails.getLastName(),
                    patientDetails.getPhoneNumber(),
                    patientDetails.getDateOfBirth(),
                    patientDetails.getPatientIdentificationNo(),
                    patientDetails.getOpenMrsId(),
//                    patientDetails.getUserKeycloakId(),
                    patientDetails.getGender(),
                    patientDetails.getUsername(),
                    patientDetails.isPatient(),
                    patientDetails.getProfileUrl(),
                    patientDetails.getNationalPassportNo()
                    );

            results = new Results(200, dbPatientDetails);

        }else {

            results = new Results(400, "User not found.");
        }

        return results;
    }

    @Override
    public Results searchUser(String emailAddress) {

        PatientDetails patientDetails = searchUserByEmail(emailAddress);
        return getPatientResultsData(patientDetails);
    }

    @Override
    public Results updateUserRole(String keyCloakId, String role) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        addRealmRoleToUser(keyCloakId, role);

        return new Results(200, new DbResults("User role has been updated successfully."));
    }

    @Override
    public Results getConditions(String userId) {

        ArrayList<DbConditionDataResults> dbConditionDataResultsArrayList = new ArrayList<>();

        PatientDetails patientDetails = getPatientData(userId);
        if (patientDetails != null){

            String openMrsId = patientDetails.getOpenMrsId();

//            if (openMrsId == null || openMrsId.isEmpty()) {
//                return new Results(200, new DbResultsData(0, new java.util.ArrayList<>()));
//            }

            List<PatientCondition> patientConditionList = networkCall
                    .getConditionsValuesDetails("2f394969-ec6d-4692-8544-4ac212d0f22e");

            patientConditionList.forEach(patientCondition -> {

                DbConditionDataResults dbConditionDataResults = new DbConditionDataResults(
                        patientCondition.getCondition(),
                        patientCondition.getClinicalStatus(),
                        patientCondition.getRecordedDate().toString(),
                        patientCondition.getRecordedBy()
                );
                dbConditionDataResultsArrayList.add(dbConditionDataResults);

            });

            DbResultsData dbResultsData = new DbResultsData(dbConditionDataResultsArrayList.size(), dbConditionDataResultsArrayList);
            return new Results(200, dbResultsData);

        }else {
            return new Results(400, new DbResults("We could not find the user."));
        }

    }

    @Override
    public Results getAllergy(String userId) {
        PatientDetails patientDetails = getPatientData(userId);
        if (patientDetails != null){

            String openMrsId = patientDetails.getOpenMrsId();
            
            if (openMrsId == null || openMrsId.isEmpty()) {
                return new Results(200, new DbResultsData(0, new java.util.ArrayList<>()));
            }
            
            Results conditionsResults = networkCall.getPatientAllergyDetails(openMrsId);

            if (conditionsResults.getCode() == 200){
                return new Results(200, conditionsResults.getMessage());
            }else {
                return new Results(400, conditionsResults.getMessage());
            }

        }else {
            return new Results(400, new DbResults("We could not find the user."));
        }
    }

    @Override
    public Results getDrugs(String userId) {
        PatientDetails patientDetails = getPatientData(userId);
        if (patientDetails != null){

            String openMrsId = patientDetails.getOpenMrsId();
            
            if (openMrsId == null || openMrsId.isEmpty()) {
                return new Results(200, new DbResultsData(0, new java.util.ArrayList<>()));
            }
            
            return networkCall.getPatientDrugDetails(openMrsId);

        }else {
            return new Results(400, new DbResults("We could not find the user."));
        }
    }

    @Override
    public Results getDrugsDetails(String userId, String drugId) {
        PatientDetails patientDetails = getPatientData(userId);
        if (patientDetails != null){

            String openMrsId = patientDetails.getOpenMrsId();
            
            if (openMrsId == null || openMrsId.isEmpty()) {
                return new Results(200, new DbResults("Linking has not been done"));
            }
            
            Results conditionsResults = networkCall.getPatientDrugDetailsData(openMrsId, drugId);
            return new Results(200, conditionsResults);

        }else {
            return new Results(400, new DbResults("We could not find the user."));
        }
    }

    @Override
    public Results getVitals(String userId) {
        PatientDetails patientDetails = getPatientData(userId);
        if (patientDetails != null){

            String openMrsId = patientDetails.getOpenMrsId();
            
            if (openMrsId == null || openMrsId.isEmpty()) {
                return new Results(200, new DbResultsData(0, new java.util.ArrayList<>()));
            }
            
            return networkCall.getPatientVitalsDetails(openMrsId);

        }else {
            return new Results(400, new DbResults("We could not find the user."));
        }
    }

    @Override
    public Results getVisits(String userId) {
        PatientDetails patientDetails = getPatientData(userId);
        if (patientDetails != null){
            String openMrsId = patientDetails.getOpenMrsId();
            
            if (openMrsId == null || openMrsId.isEmpty()) {
                return new Results(200, new DbResultsData(0, new java.util.ArrayList<>()));
            }
            
            return networkCall.getPatientVisits(openMrsId);
        } else {
            return new Results(400, new DbResults("We could not find the user."));
        }
    }

    @Override
    public Results getVisitById(String userId, String visitId) {
        PatientDetails patientDetails = getPatientData(userId);
        if (patientDetails != null){
            String openMrsId = patientDetails.getOpenMrsId();
            
            if (openMrsId == null || openMrsId.isEmpty()) {
                return new Results(200, new DbResults("Linking has not been done"));
            }
            
            return networkCall.getVisitById(visitId);
        } else {
            return new Results(400, new DbResults("We could not find the user."));
        }
    }

    @Override
    public Results refreshToken(DbRefreshToken refreshTokenRequest)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        String url = authServerUrl + refreshToken;
        String refreshToken = refreshTokenRequest.getRefreshToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("client_id", resource);
        map.add("grant_type", "refresh_token");
        map.add("refresh_token", refreshToken);
        map.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try{

            ResponseEntity<DbRefreshTokenResponse> response =
                    restTemplateConfig.restTemplate().postForEntity( url, request , DbRefreshTokenResponse.class );

            //Get code
            int code = response.getStatusCodeValue();
            if (code == 200) {
                //Get the response body
                DbRefreshTokenResponse refreshTokenResponse = response.getBody();
                if (refreshTokenResponse != null) {
                    return new Results(200, refreshTokenResponse);
                } else {
                    return new Results(400, "Could not refresh token.");
                }
            }else {
                return new Results(400, "Could not refresh token.");
            }

        }catch (Exception e){
            return new Results(400, "Invalid refresh token.");
        }

    }

    @Override
    public Results getMedicalHistory(String userId) {

        Results conditions = getConditions(userId);
        Results allergy = getAllergy(userId);
        Results drugs = getDrugs(userId);
        Results vitals = getVitals(userId);

        DbMedicalHistory dbMedicalHistory = new DbMedicalHistory();

        if (conditions.getCode() == 200){
            DbResultsData dbResultsConditionsData = (DbResultsData) conditions.getMessage();
            List<Object> conditionsData = dbResultsConditionsData.getResults();
            dbMedicalHistory.setCondition(conditionsData);
        }
        if (allergy.getCode() == 200){
            DbResultsData dbResultsAllergyData = (DbResultsData) allergy.getMessage();
            List<Object> allergyData = dbResultsAllergyData.getResults();
            dbMedicalHistory.setAllergy(allergyData);
        }
        if (drugs.getCode() == 200){
            DbResultsData dbResultsDrugsData = (DbResultsData) drugs.getMessage();
            List<Object> drugsData = dbResultsDrugsData.getResults();
            dbMedicalHistory.setDrug(drugsData);
        }
        if (vitals.getCode() == 200){
            DbResultsData dbResultsVitalsData = (DbResultsData) vitals.getMessage();
            List<Object> vitalsData = dbResultsVitalsData.getResults();
            dbMedicalHistory.setVitals(vitalsData);
        }


        return new Results(200, dbMedicalHistory);

    }

    @Override
    public Results linkPatient(LinkPatientRequest linkPatientRequest) {
        String nationalPassportNo = linkPatientRequest.getNationalPassportNo();
        String phoneNumber = linkPatientRequest.getPhoneNumber();

        // Search OpenMRS by national passport number
        Results searchResults = networkCall.searchPatientByNationalPassport(nationalPassportNo);
        
        if (searchResults.getCode() != 200) {
            String errorMessage = "Patient not found in OpenMRS with the provided national passport number";
            if (searchResults.getMessage() != null) {
                errorMessage = searchResults.getMessage().toString();
            }
            System.out.println("Link patient failed for National ID: " + nationalPassportNo + ". Error: " + errorMessage);
            return new Results(400, errorMessage);
        }

        // Extract patient information from OpenMRS
        DbOpenMrsPatientSearchResult openMrsPatient = (DbOpenMrsPatientSearchResult) searchResults.getMessage();
        String openMrsPhoneNumber = openMrsPatient.getPhoneNumber();
        
        // Normalize phone numbers (remove spaces) before comparison
        String normalizedOpenMrsPhone = normalizePhoneNumber(openMrsPhoneNumber);
        String normalizedProvidedPhone = normalizePhoneNumber(phoneNumber);
        
        // Verify phone number matches
        if (normalizedOpenMrsPhone == null || !normalizedOpenMrsPhone.equals(normalizedProvidedPhone)) {
            return new Results(400, "Phone number does not match the one in OpenMRS");
        }

        // Find patient in local database by national passport number
        PatientDetails patientDetails = patientDetailsRepository.findByNationalPassportNo(nationalPassportNo);
        
        if (patientDetails == null) {
            return new Results(400, "Patient not found in local database with the provided national passport number");
        }

        // Check if patient is already linked
        boolean isAlreadyLinked = patientDetails.isPatient() && 
                                   patientDetails.getOpenMrsId() != null && 
                                   !patientDetails.getOpenMrsId().isEmpty();
        
        if (isAlreadyLinked) {
            // Patient is already linked, return patient info with already linked message
            Results patientResults = getPatientResultsData(patientDetails);
            if (patientResults.getCode() == 200) {
                DbPatientDetails dbPatientDetails = (DbPatientDetails) patientResults.getMessage();
                LinkPatientResponse linkResponse = new LinkPatientResponse(
                    "Patient is already linked",
                    dbPatientDetails,
                    true
                );
                return new Results(200, linkResponse);
            } else {
                return new Results(400, "Failed to retrieve patient details");
            }
        }

        // Update patient identification number with OpenMRS ID
        String openMrsId = openMrsPatient.getOpenMrsId();
        patientDetails.setPatientIdentificationNo(openMrsId);
        patientDetails.setOpenMrsId(openMrsPatient.getOpenMrsUuid());
        patientDetails.setPatient(true);

        // Save the updated patient details
        PatientDetails updatedPatientDetails = updateUserDetails(patientDetails);
        if (updatedPatientDetails != null) {
            // Return patient info with success message
            Results patientResults = getPatientResultsData(updatedPatientDetails);
            if (patientResults.getCode() == 200) {
                DbPatientDetails dbPatientDetails = (DbPatientDetails) patientResults.getMessage();
                LinkPatientResponse linkResponse = new LinkPatientResponse(
                    "Patient linked successfully",
                    dbPatientDetails,
                    false
                );
                return new Results(200, linkResponse);
            } else {
                return new Results(400, "Failed to retrieve patient details");
            }
        } else {
            return new Results(400, "Failed to update patient details");
        }
    }

    @Override
    public Results unlinkPatient(LinkPatientRequest linkPatientRequest) {
        String nationalPassportNo = linkPatientRequest.getNationalPassportNo();
        String phoneNumber = linkPatientRequest.getPhoneNumber();

        // Search OpenMRS by national passport number (for verification)
        Results searchResults = networkCall.searchPatientByNationalPassport(nationalPassportNo);
        
        if (searchResults.getCode() != 200) {
            String errorMessage = searchResults.getMessage() != null ? 
                searchResults.getMessage().toString() : 
                "Patient not found in OpenMRS with the provided national passport number";
            return new Results(400, errorMessage);
        }

        // Extract patient information from OpenMRS
        DbOpenMrsPatientSearchResult openMrsPatient = (DbOpenMrsPatientSearchResult) searchResults.getMessage();
        String openMrsPhoneNumber = openMrsPatient.getPhoneNumber();
        
        // Normalize phone numbers (remove spaces) before comparison
        String normalizedOpenMrsPhone = normalizePhoneNumber(openMrsPhoneNumber);
        String normalizedProvidedPhone = normalizePhoneNumber(phoneNumber);
        
        // Verify phone number matches
        if (normalizedOpenMrsPhone == null || !normalizedOpenMrsPhone.equals(normalizedProvidedPhone)) {
            return new Results(400, "Phone number does not match the one in OpenMRS");
        }

        // Find patient in local database by national passport number
        PatientDetails patientDetails = patientDetailsRepository.findByNationalPassportNo(nationalPassportNo);
        
        if (patientDetails == null) {
            return new Results(400, "Patient not found in local database with the provided national passport number");
        }

        // Clear linking: set isPatient = false, openMrsId = null, patientIdentificationNo = null
        patientDetails.setPatient(false);
        patientDetails.setOpenMrsId(null);
        patientDetails.setPatientIdentificationNo(null);

        // Save the updated patient details
        PatientDetails updatedPatientDetails = updateUserDetails(patientDetails);
        if (updatedPatientDetails != null) {
            return new Results(200, new DbResults("Patient unlinked successfully"));
        } else {
            return new Results(400, "Failed to update patient details");
        }
    }

    /**
     * Normalize phone number by removing all spaces
     * @param phoneNumber The phone number to normalize
     * @return Normalized phone number without spaces, or null if input is null
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;
        return phoneNumber.replaceAll("\\s+", "");
    }

    public void addRealmRoleToUser(String keyCloakId, String role_name)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        Keycloak keycloak = KeycloakBuilder
                .builder()
                .serverUrl(authServerUrl)
                .grantType(CLIENT_CREDENTIALS)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                // .username(username).password(password)
                .resteasyClient(
                        new ResteasyClientBuilder()
                                .sslContext(new SSLContextBuilder()
                                        .loadTrustMaterial(null,
                                                TrustAllStrategy.INSTANCE).build())
                                .hostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .connectionPoolSize(10).build())
                .build();

        //Get roles in realm
        List<RoleRepresentation> roles = keycloak.realm(realm).roles().list();

        //Assign role to user
        for (RoleRepresentation role : roles) {
            if (role.getName().equals(role_name)) {

                keycloak.realm(realm)
                        .users()
                        .get(keyCloakId)
                        .roles()
                        .realmLevel()
                        .add(
                                Arrays.asList(role)
                        );
            }
        }

    }

    @NotNull
    private Results getPatientResultsData(PatientDetails patientDetails) {
        if (patientDetails != null){
            DbPatientDetails dbPatientDetails = new DbPatientDetails(
                    patientDetails.getId(),
                    patientDetails.getEmailAddress(),
                    patientDetails.getFirstName(),
                    patientDetails.getLastName(),
                    patientDetails.getPhoneNumber(),
                    patientDetails.getDateOfBirth(),
                    patientDetails.getPatientIdentificationNo(),
                    patientDetails.getOpenMrsId(),
//                    patientDetails.getUserKeycloakId(),
                    patientDetails.getGender(),
                    patientDetails.getUsername(),
                    patientDetails.isPatient(),
                    patientDetails.getProfileUrl(),
                    patientDetails.getNationalPassportNo()
            );
            return new Results(200, dbPatientDetails);
        }else {
            return new Results(400, "User not found.");
        }
    }

    //Search for a user by email address
    private PatientDetails searchUserByEmail(String emailAddress){

        boolean isEmailExist = patientDetailsRepository.existsByEmailAddress(emailAddress);
        if (isEmailExist) {
            return patientDetailsRepository.findByEmailAddress(emailAddress);
        }else {
            return null;
        }

    }

    public PatientDetails updateUserDetails(PatientDetails patientDetails){

        return patientDetailsRepository.findById(patientDetails.getId())
                .map(patientDetailsOld ->{
                    patientDetailsOld.setFirstName(patientDetails.getFirstName());
                    patientDetailsOld.setLastName(patientDetails.getLastName());
                    patientDetailsOld.setPhoneNumber(patientDetails.getPhoneNumber());
                    patientDetailsOld.setUsername(patientDetails.getUsername());
                    patientDetailsOld.setProfileUrl(patientDetails.getProfileUrl());

                    patientDetailsOld.setPatientIdentificationNo(patientDetails.getPatientIdentificationNo());
                    patientDetailsOld.setPatient(patientDetails.isPatient());
                    patientDetailsOld.setOpenMrsId(patientDetails.getOpenMrsId());
                    return patientDetailsRepository.save(patientDetailsOld);
                }).orElse(null);


    }


    public void createNotification(DbNotification dbNotification){

        networkCall.createNotification("CREATE_NOTIFICATION", dbNotification);

    }

    //get reset otp
    private DbVerificationLink getResetPassword(PatientDetails patientDetails){

        String otpCode = formatterClass.getResetPasswordOtp(6);
        patientDetails.setOneTimeLink(otpCode);
        patientDetails.setOtpRequestedTime(new Date());

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return new DbVerificationLink(baseUrl, otpCode, patientDetails);

    }


    //get verification link
    private DbVerificationLink generateVerificationLink(PatientDetails patientDetails){

        String verificationLink = formatterClass.getVerificationLink(30);

        patientDetails.setOneTimeLink(verificationLink);
        patientDetails.setOtpRequestedTime(new Date());

        String baseUrl = "https://172.105.157.130/";

        return new DbVerificationLink(baseUrl, verificationLink, patientDetails);

    }

    private PatientDetails findByEmailAddress(String emailAdress){
        return patientDetailsRepository.findByEmailAddress(emailAdress);
    }

    private PatientDetails updatePatientDetails(PatientDetails patientDetails){

        String id = patientDetails.getId();
        return patientDetailsRepository.findById(id)
                .map(patientDetailsOld -> {

                    patientDetailsOld.setVerified(patientDetails.isVerified());

                    patientDetailsOld.setOneTimeLink(patientDetails.getOneTimeLink());
                    patientDetailsOld.setOtpRequestedTime(patientDetails.getOtpRequestedTime());

                    return patientDetailsRepository.save(patientDetailsOld);

                }).orElse(null);
    }

    private KeycloakUserId createKeycloakUser(RegisterRequest registerRequest) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {

        String role = "app-user";

        // Install the all-trusting trust manager
        Keycloak keycloak = KeycloakBuilder
                .builder()
                .serverUrl(authServerUrl)
                .grantType(CLIENT_CREDENTIALS)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                // .username(username).password(password)
                .resteasyClient(
                        new ResteasyClientBuilder()
                                .sslContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                                .hostnameVerifier(NoopHostnameVerifier.INSTANCE).connectionPoolSize(100).build())
                .build();

        keycloak.tokenManager().getAccessToken();

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(registerRequest.getEmailAddress());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmailAddress());
        user.setEmailVerified(true);

        // Get realm
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersRessource = realmResource.users();

        Response response = usersRessource.create(user);

        int statusCode = response.getStatus();

        if (statusCode == 201){
            //User has been registered successfully, get saved details
            String userId = CreatedResponseUtil.getCreatedId(response);

            // create password credential
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(registerRequest.getPassword());

            UserResource userResource = usersRessource.get(userId);
//            // Set password credential
            userResource.resetPassword(passwordCred);

            addRealmRoleToUser(userId, role);

            return new KeycloakUserId(userId);


        }else {

            return new KeycloakUserId("");
        }


    }


    private void assignRoleToUser(String userId, String role) {

        Keycloak keycloak = getAdminKeycloak();
        UsersResource usersResource = keycloak.realm(realm).users();
        UserResource userResource = usersResource.get(userId);

        //getting client
        ClientRepresentation clientRepresentation =
                keycloak.realm(realm).clients().findAll().stream().filter(
                        client -> client.getClientId().equals(clientId))
                        .collect(Collectors.toList()).get(0);
        ClientResource clientResource = keycloak.realm(realm).clients().get(clientRepresentation.getId());
        //getting role
        RoleRepresentation roleRepresentation = clientResource.roles().list().stream().filter(element -> element.getName().equals(role)).collect(Collectors.toList()).get(0);
        //assigning to user
        userResource.roles().clientLevel(clientRepresentation.getId()).add(Collections.singletonList(roleRepresentation));
    }

    private Keycloak getAdminKeycloak() {
        return KeycloakBuilder.builder().serverUrl(authServerUrl)
                .realm(realm)
                .clientId(clientId)
                .username(username)
                .password(password)
                .grantType(PASSWORD)
                .clientSecret(clientSecret)
                .resteasyClient(
                        new ResteasyClientBuilder()
                                .connectionPoolSize(10).build()
                ).build();
    }

    private Boolean checkEmailAddress(String emailAddress) {

        return patientDetailsRepository.existsByEmailAddress(emailAddress);
    }



}
