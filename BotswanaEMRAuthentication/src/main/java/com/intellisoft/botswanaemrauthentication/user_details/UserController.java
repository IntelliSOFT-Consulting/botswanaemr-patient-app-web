package com.intellisoft.botswanaemrauthentication.user_details;

import com.intellisoft.botswanaemrauthentication.DbConsent;
import com.intellisoft.botswanaemrauthentication.FormatterClass;
import com.intellisoft.botswanaemrauthentication.Results;
import com.intellisoft.botswanaemrauthentication.UpdateUserDetails;
import com.intellisoft.botswanaemrauthentication.authentication.service_class.PatientDetailsService;
import com.intellisoft.botswanaemrauthentication.consent.service.ConsentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping(value = "/auths/api/v1/user/")
@RestController
public class UserController {

    @Autowired
    private PatientDetailsService patientDetailsService;
    FormatterClass formatterClass = new FormatterClass();
    @GetMapping(value = "details")
    public ResponseEntity<?> getUserDetails(){

        Optional<String> userId = formatterClass.getCurrentUserLogin();

        try {

            Results results;
            if (userId.isPresent()){
                results = patientDetailsService.getUserDetails(userId.get());
            }else {
                results = new Results(400, "We could not find the user");
            }
            return formatterClass.getResponse(results);

        }catch (Exception e){

            Results results = new Results(400, " Please try again after sometime: " + e.toString());
            return formatterClass.getResponse(results);

        }



    }

    @PutMapping(value = "details")
    public ResponseEntity<?> updateUserDetails(
            @RequestBody UpdateUserDetails userDetails){

        try{

            Optional<String> userId = formatterClass.getCurrentUserLogin();
            Results results;
            if (userId.isPresent()){
                results = patientDetailsService.updatePatientInfo(userDetails, userId.get());
            }else {
                results = new Results(400, "We could not find the user");
            }
            return formatterClass.getResponse(results);

        }catch (Exception e){
            Results results = new Results(400, "Please check if you have provided a profile picture.");
            return ResponseEntity.badRequest().body(results);
        }


    }

    /**
     * Get Conditions
     */
    @GetMapping(value = "conditions")
    public ResponseEntity<?> getPatientConditions(){

        Optional<String> userId = formatterClass.getCurrentUserLogin();

        try {

            Results results;
            if (userId.isPresent()){
                results = patientDetailsService.getConditions(userId.get());
            }else {
                results = new Results(400, "We could not find the user");
            }
            return formatterClass.getResponse(results);

        }catch (Exception e){
            Results results = new Results(400, " Please try again after sometime: " + e.toString());
            return formatterClass.getResponse(results);

        }

    }

    /**
     * Get Allergy
     */
    @GetMapping(value = "allergy")
    public ResponseEntity<?> getAllergy(){

        Optional<String> userId = formatterClass.getCurrentUserLogin();

        try {

            Results results;
            if (userId.isPresent()){
                results = patientDetailsService.getAllergy(userId.get());
            }else {
                results = new Results(400, "We could not find the user");
            }
            return formatterClass.getResponse(results);

        }catch (Exception e){
            Results results = new Results(400, " Please try again after sometime.");
            return formatterClass.getResponse(results);

        }

    }

    /**
     * Get Drugs
     */
    @GetMapping(value = "drugs")
    public ResponseEntity<?> getDrugs(){

        Optional<String> userId = formatterClass.getCurrentUserLogin();

        try {

            Results results;
            if (userId.isPresent()){
                results = patientDetailsService.getDrugs(userId.get());
            }else {
                results = new Results(400, "We could not find the user");
            }
            return formatterClass.getResponse(results);

        }catch (Exception e){
            Results results = new Results(400, " Please try again after sometime.");
            return formatterClass.getResponse(results);

        }

    }/**
     * Get Drugs Details
     */
    @GetMapping(value = "drugs/{drugId}")
    public ResponseEntity<?> getDrugs(@PathVariable("drugId") String drugId){

        Optional<String> userId = formatterClass.getCurrentUserLogin();

        try {

            Results results;
            if (userId.isPresent()){
                results = patientDetailsService.getDrugsDetails(userId.get(), drugId);
            }else {
                results = new Results(400, "We could not find the user");
            }
            return formatterClass.getResponse(results);

        }catch (Exception e){
            Results results = new Results(400,
                    " Please try again after sometime.");
            return formatterClass.getResponse(results);

        }

    }

    //Get combined conditions, allergy and drugs
    @GetMapping(value = "medical-history")
    public ResponseEntity<?> getMedicalHistory(){

        Optional<String> userId = formatterClass.getCurrentUserLogin();

        try {

            Results results;
            if (userId.isPresent()){
                results = patientDetailsService.getMedicalHistory(userId.get());
            }else {
                results = new Results(400, "We could not find the user");
            }
            return formatterClass.getResponse(results);

        }catch (Exception e){
            e.printStackTrace();
            Results results = new Results(400,
                    " Please try again after sometime.");
            return formatterClass.getResponse(results);

        }

    }

}
