package com.intellisoft.botswanaemrambulance.backoffice.controller;

import com.intellisoft.botswanaemrambulance.AmbulanceIncidentStatus;
import com.intellisoft.botswanaemrambulance.DbAmbulance;
import com.intellisoft.botswanaemrambulance.FormatterClass;
import com.intellisoft.botswanaemrambulance.Results;
import com.intellisoft.botswanaemrambulance.backoffice.service_impl.impl.BackofficeAmbulanceServiceImpl;
import com.intellisoft.botswanaemrambulance.backoffice.service_impl.service.BackofficeAmbulanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Tag(description = "Use these resource to save the Ambulance dispatch team, Add Ambulances and drivers to the system.", name = "Ambulance module")
@RequestMapping(value = "/ambulance/api/v1/backoffice")
@RestController
public class BackofficeAmbulanceController {

    @Autowired
    private BackofficeAmbulanceService backofficeAmbulanceService;

    private FormatterClass formatterClass = new FormatterClass();

    //Add Ambulance
    @Operation(summary = "Create an ambulance", description = "Add an ambulance to the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @PostMapping(path = "/create-ambulance")
    public ResponseEntity<?> addAmbulance(@RequestBody DbAmbulance dbAmbulance) {

        Results results = backofficeAmbulanceService.addAmbulance(dbAmbulance);
        return formatterClass.getResponse(results);

    }

    //Get Ambulance Details
    @Operation(summary = "Get ambulance details", description = "View Ambulance details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @GetMapping(value = "/ambulance-details/{ambulanceId}")
    public ResponseEntity<?> getAmbulanceDetails(@PathVariable("ambulanceId") String ambulanceId){

        Results results = backofficeAmbulanceService.getAmbulance(ambulanceId);
        return formatterClass.getResponse(results);

    }

    //Get All Ambulances
    @Operation(summary = "List Ambulances", description = "Get a list of all ambulances filtering using the status of the ambulance, the page number and number of ambulances to be displayed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @GetMapping(value = "/view-ambulances")
    public ResponseEntity<?> getPagedAmbulances(
            @Param("pageNo") int pageNo,
            @Param("itemNo") int itemNo,
            @Param("ambulanceStatus") String ambulanceStatus
    ){
        String status = "";
        if (ambulanceStatus != null){

            if (ambulanceStatus.equals("Available")){
                status = AmbulanceIncidentStatus.AVAILABLE.name();
            }
            if (ambulanceStatus.equals("Dispatched")){
                status = AmbulanceIncidentStatus.DISPATCHED.name();
            }
            if (ambulanceStatus.equals("Offline")){
                status = AmbulanceIncidentStatus.OFFLINE.name();
            }

        }else {
            status = AmbulanceIncidentStatus.AVAILABLE.name();
        }

        Results results = backofficeAmbulanceService.getAmbulances(pageNo,itemNo, status);
        return formatterClass.getResponse(results);

    }

    //Update Ambulance
    @Operation(summary = "Update Ambulances", description = "Update an ambulance details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @PutMapping(value = "/update-ambulance")
    public ResponseEntity<?> updateAmbulanceDetails(
            @RequestBody DbAmbulance dbAmbulance
    ){

        Results results = backofficeAmbulanceService.updateAmbulanceDetails(dbAmbulance);
        return formatterClass.getResponse(results);

    }

}
