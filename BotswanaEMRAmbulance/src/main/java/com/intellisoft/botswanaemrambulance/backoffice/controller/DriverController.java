package com.intellisoft.botswanaemrambulance.backoffice.controller;

import com.intellisoft.botswanaemrambulance.AmbulanceIncidentStatus;
import com.intellisoft.botswanaemrambulance.DriverStatus;
import com.intellisoft.botswanaemrambulance.FormatterClass;
import com.intellisoft.botswanaemrambulance.Results;
import com.intellisoft.botswanaemrambulance.backoffice.entity.DriverDetails;
import com.intellisoft.botswanaemrambulance.backoffice.service_impl.impl.DriverServiceImpl;
import com.intellisoft.botswanaemrambulance.backoffice.service_impl.service.DriverService;
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
@Tag(description = "Use these resource to save the Ambulance driver team, Add driver and drivers to the system.", name = "Ambulance Driver module")
@RequestMapping(value = "/ambulance/api/v1/backoffice")
@RestController
public class DriverController {

    @Autowired
    private DriverServiceImpl driverService;
    private FormatterClass formatterClass = new FormatterClass();

    //Search user from acl using email address
    @Operation(summary = "Search user", description = "Check if the provided email address exists in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @RequestMapping(value = "/search-user", method = RequestMethod.GET)
    public ResponseEntity<?> addDriver(
            @Param("emailAddress") String emailAddress
    ) {

        Results results = driverService.getUsers(emailAddress);
        return formatterClass.getResponse(results);

    }

    //Update Driver from user
    @Operation(summary = "Upgrade a user ", description = "Upgrade a user to being a driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @RequestMapping(value = "/update-driver/{driverId}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateUserToDriver(@PathVariable("driverId") String driverId) {

        Results results = driverService.updateUserToDriver(driverId);
        return formatterClass.getResponse(results);

    }

    //Get Driver List
    @Operation(summary = "Get Drivers", description = "Get a list of all drivers, filtering using the number of pages, number of items required and the availability of the driver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @RequestMapping(value = "/drivers", method = RequestMethod.GET)
    public ResponseEntity<?> getDriversList(
            @Param("pageNo") int pageNo,
            @Param("itemNo") int itemNo,
            @Param("driverStatus") String driverStatus
    ) {

        boolean hasAmbulance = false;
        if (driverStatus != null){

            if (driverStatus.equals("Assigned")){
                hasAmbulance = true;
            }

        }

        Results results = driverService.getDriversList(pageNo, itemNo, "hasAmbulance", hasAmbulance);
        return formatterClass.getResponse(results);

    }

    //Get Driver Details
    @Operation(summary = "Driver Details", description = "Get the driver details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @RequestMapping(value = "/driver/{driverId}", method = RequestMethod.GET)
    public ResponseEntity<?> getDriverDetails(@PathVariable("driverId") String driverId) {

        Results results = driverService.getDriver(driverId);
        return formatterClass.getResponse(results);

    }



}
