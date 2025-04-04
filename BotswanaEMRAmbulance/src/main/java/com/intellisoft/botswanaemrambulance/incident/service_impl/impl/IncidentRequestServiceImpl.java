package com.intellisoft.botswanaemrambulance.incident.service_impl.impl;

import com.intellisoft.botswanaemrambulance.*;
import com.intellisoft.botswanaemrambulance.backoffice.entity.BackofficeAmbulance;
import com.intellisoft.botswanaemrambulance.backoffice.service_impl.impl.BackofficeAmbulanceServiceImpl;
import com.intellisoft.botswanaemrambulance.incident.entity.IncidentRejections;
import com.intellisoft.botswanaemrambulance.incident.entity.IncidentRequest;
import com.intellisoft.botswanaemrambulance.incident.repository.IncidentRejectionsRepository;
import com.intellisoft.botswanaemrambulance.incident.repository.IncidentRequestRepository;
import com.intellisoft.botswanaemrambulance.incident.service_impl.service.IncidentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Service
public class IncidentRequestServiceImpl implements IncidentRequestService {

    @Autowired
    private IncidentRequestRepository incidentRequestRepository;

    @Autowired
    private BackofficeAmbulanceServiceImpl backofficeAmbulanceService;

    @Autowired
    private IncidentRejectionsRepository incidentRejectionsRepository;

    @Override
    public Results createIncidentRequest(DbIncidentRequest dbIncidentRequest) {

        String username = dbIncidentRequest.getUserName();
        String pickUpLocation = dbIncidentRequest.getLocation();
        String incident = dbIncidentRequest.getIncident();
        String additionalInformation = dbIncidentRequest.getAdditionalInformation();
        String phoneNumber = dbIncidentRequest.getPhoneNumber();

        String incidentStatus = IncidentStatus.AWAITING.name();

        IncidentRequest incidentRequest = new IncidentRequest(
                username, pickUpLocation, null,
                phoneNumber, incident,
                additionalInformation,incidentStatus, null);

        incidentRequestRepository.save(incidentRequest);
        return new Results(200, new DbResults("The incident has been saved."));
    }

    @Override
    public Results getIncidents(int pageNo, int itemNo, String status) {
        List<IncidentRequest> incidentRequestList = new ArrayList<>();
        if (status.equals("ALL")){
            incidentRequestList = getPaginatedData(pageNo-1, itemNo, "", "");
        }else {
            incidentRequestList = incidentRequestRepository.findAllByIncidentStatus(
                    status,
                    PageRequest.of(pageNo-1, itemNo));
        }

        String nextUrl = null;
        String previousUrl = null;

        if(pageNo > 1){
            previousUrl = getUrlDetails(status, pageNo-1, itemNo);
        }else{
            nextUrl = getUrlDetails(status, pageNo+1, itemNo);
        }

        DbDataResults dbDataResults = new DbDataResults(
                incidentRequestList.size(),
                nextUrl,
                previousUrl,
                incidentRequestList);


        return new Results(200, dbDataResults);
    }

    private String getUrlDetails(String status, int pageNo, int itemNo){

        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequestUri();
        builder.scheme("https");

        String statusValue = "";
        if (status.equals("ALL")){
            statusValue = "";
        }else {
            statusValue = status;
        }
        builder.replaceQueryParam("incidentStatus", statusValue);
        builder.replaceQueryParam("itemNo", itemNo);
        builder.replaceQueryParam("pageNo", pageNo);

        URI nextUrl = builder.build().toUri();
        return String.valueOf(nextUrl);

    }

    private List<IncidentRequest> getPaginatedData(int pageNo, int pageSize, String sortField, String sortDirection){

        String sortPageField = "";
        String sortPageDirection = "";

        if (sortField.equals("")){sortPageField = "createdAt";}else {sortPageField = sortField;}
        if (sortDirection.equals("")){sortPageDirection = "DESC";}else {sortPageDirection = sortField;}

        Sort sort = sortPageDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortPageField).ascending() :
                Sort.by(sortPageField).descending();

        Pageable pageable = PageRequest.of(pageNo , pageSize, sort);
        Page<IncidentRequest> page = incidentRequestRepository.findAll(pageable);
        return page.getContent();

    }



    @Override
    public Results getIncidentDetails(String id) {

        IncidentRequest incidentRequest = getIncidentData(id);
        if (incidentRequest != null){
            return new Results(200, incidentRequest);
        }else {
            return new Results(400, "The requested resource could not be found.");
        }

    }

    private IncidentRequest getIncidentData(String id){

        Optional<IncidentRequest> optionalData = incidentRequestRepository.findById(id);
        return optionalData.orElse(null);

    }

    @Override
    public Results updateIncidentDetails(String id, DbUpdateIncidentRequest dbUpdateIncidentRequest) {

        String incidentStatus = dbUpdateIncidentRequest.getIncidentStatus();
        String status = IncidentStatus.AWAITING.name();

        if (incidentStatus != null){

            if (incidentStatus.equals("Awaiting")){
                status = IncidentStatus.AWAITING.name();
            }
            if (incidentStatus.equals("OnRoute")){
                status = IncidentStatus.ON_ROUTE.name();
            }
            if (incidentStatus.equals("InHospital")){
                status = IncidentStatus.IN_HOSPITAL.name();
            }

        }
        String additionalInformation = dbUpdateIncidentRequest.getAdditionalInformation();

        DbUpdateIncidentRequest updateDbUpdateIncidentRequest = new DbUpdateIncidentRequest(additionalInformation, status);

        IncidentRequest incidentRequest = updateIncident(id, updateDbUpdateIncidentRequest);
        if (incidentRequest != null){
            return new Results(200, "Incident has been updated.");
        }else {
            return new Results(400, "The incident could not be updated.");
        }
        
    }

    @Override
    public Results searchPatient(String patientName) {



        return null;
    }

    private IncidentRequest updateIncident(String id, DbUpdateIncidentRequest dbUpdateIncidentRequest){

        return incidentRequestRepository.findById(id)
                .map(incidentRequestOld ->{
                    incidentRequestOld.setIncidentStatus(dbUpdateIncidentRequest.getIncidentStatus());
                    incidentRequestOld.setAdditionalInformation(dbUpdateIncidentRequest.getAdditionalInformation());
                    return incidentRequestRepository.save(incidentRequestOld);
                }).orElse(null);

    }

    @Override
    public Results assignAmbulanceIncident(DbAcceptRequest dbAcceptRequest) {

        String incidentId = dbAcceptRequest.getIncidentId();
        String ambulanceRegNo = dbAcceptRequest.getAmbulanceRegNo();
        String dropOffLocation = dbAcceptRequest.getDropOffLocation();
        String pickupLocation = dbAcceptRequest.getPickUpLocation();

        BackofficeAmbulance backofficeAmbulance = backofficeAmbulanceService.findByRegNo(ambulanceRegNo);

        IncidentRequest incidentRequest = getIncidentData(incidentId);
        if (incidentRequest != null && backofficeAmbulance !=null){

            /**
             * Check if ambulance is available, update incident status and assign ambulance
             * Update ambulance status to on route
             */
            String backofficeAmbulanceId = backofficeAmbulance.getId();
            String backofficeAmbulanceStatus = backofficeAmbulance.getStatus();

            if(backofficeAmbulanceStatus.equals(AmbulanceIncidentStatus.AVAILABLE.name())){
                //Ambulance is available for dispatch
                incidentRequest.setIncidentStatus(IncidentStatus.ON_ROUTE.name());
                incidentRequest.setBackofficeAmbulanceId(backofficeAmbulanceId);
                incidentRequest.setpickUpLocation(pickupLocation);
                incidentRequest.setdropOffLocation(dropOffLocation);

                DbUpdateIncidentRequest dbUpdateIncidentRequest = new DbUpdateIncidentRequest(
                        incidentRequest.getAdditionalInformation(),
                        incidentRequest.getIncidentStatus());

                updateIncident(incidentId, dbUpdateIncidentRequest);

                //Update ambulance status to Dispatched
                backofficeAmbulance.setStatus(AmbulanceIncidentStatus.DISPATCHED.name());
                backofficeAmbulanceService.updateAmbulance(backofficeAmbulanceId, backofficeAmbulance);

                return new Results(200, new DbResults("Ambulance has been assigned to the incident."));

            }else {

                return new Results(400, "Ambulance is not available.");

            }


        }else {
            return new Results(400, "We cannot assign an incident and an ambulance.");
        }

    }

    @Override
    public Results rejectAmbulanceIncident(DbRejectRequest dbRejectRequest) {

        String incidentId = dbRejectRequest.getIncidentId();
        String reason = dbRejectRequest.getReason();

        IncidentRejections incidentRejections = new IncidentRejections(incidentId, reason);
        incidentRejectionsRepository.save(incidentRejections);
        return new Results(200, new DbResults("Incident has been rejected."));


    }


}
