package com.intellisoft.botswanaemrambulance.incident.controller;

import com.intellisoft.botswanaemrambulance.*;
import com.intellisoft.botswanaemrambulance.incident.service_impl.impl.IncidentRequestServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(description = "Use these resource to save the Ambulance incidents.", name = "Ambulance Incident module")
@RequestMapping(value = "/ambulance/api/v1/incident-request")
@RestController
public class IncidentRequestController {

    @Autowired
    private IncidentRequestServiceImpl incidentRequestService;

    private FormatterClass formatterClass = new FormatterClass();

    //Add incident request
    @Operation(summary = "Add incident request", description = "Add incident request")
    @PostMapping(path = "/create")
    public ResponseEntity<?> addIncident(@RequestBody DbIncidentRequest dbIncidentRequest) {

        Results results = incidentRequestService.createIncidentRequest(dbIncidentRequest);
        return formatterClass.getResponse(results);

    }

    //Get incident request
    @Operation(summary = "Get incident request", description = "Get incident request")
    @GetMapping(value = "/{incidentId}")
    public ResponseEntity<?> getIncident(
            @PathVariable("incidentId") String incidentId){

        Results results = incidentRequestService.getIncidentDetails(incidentId);
        return formatterClass.getResponse(results);

    }

    //Get All incident requests
    @Operation(summary = "Get all incident requests", description = "Get all incident requests")
    @GetMapping()
    public ResponseEntity<?> getPagedAmbulances(
            @RequestParam("pageNo") int pageNo,
            @RequestParam("itemNo") int itemNo,
            @RequestParam(value = "incidentStatus", required = false) String incidentStatus
    ){
        String incidentStatusValue = "";
        if (incidentStatus == null || incidentStatus.equals("")){
            incidentStatusValue = "ALL";
        }else {
            incidentStatusValue = incidentStatus;
        }

        Results results = incidentRequestService.getIncidents(pageNo,itemNo, incidentStatusValue);
        return formatterClass.getResponse(results);

    }

    //Assign Ambulance to Incident
    @Operation(summary = "Assign Ambulance to Incident", description = "Assign Ambulance to Incident")
    @PostMapping(value = "/approve-request")
    public ResponseEntity<?> assignAmbulance(
            @RequestBody DbAcceptRequest dbAcceptRequest
    ){

    	Results results = incidentRequestService.assignAmbulanceIncident(dbAcceptRequest);
        return formatterClass.getResponse(results);
    }

    //Reject Ambulance Request
    @Operation(summary = "Reject Ambulance Request", description = "Reject Ambulance Request")
    @PostMapping(value = "/reject-request")
    public ResponseEntity<?> rejectAmbulance(
            @RequestBody DbRejectRequest dbRejectRequest
    ){

    	Results results = incidentRequestService.rejectAmbulanceIncident(dbRejectRequest);
        return formatterClass.getResponse(results);
    }



}
