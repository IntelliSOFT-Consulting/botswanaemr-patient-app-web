package com.intellisoft.botswanaemrambulance.backoffice.controller;

import com.intellisoft.botswanaemrambulance.FormatterClass;
import com.intellisoft.botswanaemrambulance.Results;
import com.intellisoft.botswanaemrambulance.backoffice.service_impl.impl.DispatchTeamServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(description = "Use these resource to save dispatchers in the system.", name = "Ambulance Dispatcher module")
@RequestMapping(value = "/ambulance/api/v1/backoffice")
@RestController
public class DispatchTeamController {

    @Autowired
    private DispatchTeamServiceImpl dispatchTeamService;
    private FormatterClass formatterClass = new FormatterClass();

    //Update dispatchTeam from user
    @Operation(summary = "Update Dispatcher", description = "Update a created dispatcher details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @RequestMapping(value = "/update-dispatcher/{dispatchTeamId}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateUserToDispatchTeam(@PathVariable("dispatchTeamId") String dispatchTeamId) {

        Results results = dispatchTeamService.updateUserToDispatchTeam(dispatchTeamId);
        return formatterClass.getResponse(results);

    }

    //Get dispatchTeam List
    @Operation(summary = "Dispatcher List", description = "Display a list of the dispatchers filtering using the page number and number of items to be returned.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @RequestMapping(value = "/dispatch/", method = RequestMethod.GET)
    public ResponseEntity<?> getDispatchTeamsList(
            @Param("pageNo") int pageNo,
            @Param("itemNo") int itemNo) {

        Results results = dispatchTeamService.getDispatchTeamsList(pageNo, itemNo, "", "");
        return formatterClass.getResponse(results);

    }

    //Get dispatchTeam Details
    @Operation(summary = "Dispatcher's Details", description = "Get a detailed information about a dispatcher.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @RequestMapping(value = "/dispatch/{dispatchTeamId}", method = RequestMethod.GET)
    public ResponseEntity<?> getDispatchTeamDetails(@PathVariable("dispatchTeamId") String dispatchTeamId) {

        Results results = dispatchTeamService.getDispatchTeam(dispatchTeamId);
        return formatterClass.getResponse(results);

    }

    //Assign Driver to ambulance
    @Operation(summary = "Assign a driver to an ambulance", description = "Once an ambulance is registered, its needs to have a driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @RequestMapping(value = "/assign-driver/", method = RequestMethod.POST)
    public ResponseEntity<?> assignDriverToAmbulance(
            @Param("ambulanceId") String ambulanceId,
            @Param("driverId") String driverId) {

        Results results = dispatchTeamService.assignAmbulanceDriver(ambulanceId, driverId);
        return formatterClass.getResponse(results);

    }


}
