package com.intellisoft.botswanaemrambulance.incident.service_impl.service;

import com.intellisoft.botswanaemrambulance.*;

public interface IncidentRequestService {

    Results createIncidentRequest(DbIncidentRequest dbIncidentRequest);
    Results getIncidents(int pageNo, int itemNo, String status);
    Results getIncidentDetails(String id);
    Results updateIncidentDetails(String id, DbUpdateIncidentRequest dbUpdateIncidentRequest);
    Results searchPatient(String patientName);
    Results assignAmbulanceIncident(DbAcceptRequest dbAcceptRequest);
    Results rejectAmbulanceIncident(DbRejectRequest dbRejectRequest);

}
