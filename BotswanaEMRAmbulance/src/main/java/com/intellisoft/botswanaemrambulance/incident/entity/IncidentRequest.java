package com.intellisoft.botswanaemrambulance.incident.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intellisoft.botswanaemrambulance.backoffice.entity.BackofficeAmbulance;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "incident_request")
public class IncidentRequest {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(
                            name = "uui_gen_strategy_class",
                            value = "org.hibernate.id.uuid.CustomVersionOneStrategy"
                    )
            }
    )
    private String id;
    private String userName;
    private String pickUpLocation;
    private String dropOffLocation;
    private String phoneNumber;

    private String incident;
    private String additionalInformation;
    private String incidentStatus;

    private String backofficeAmbulanceId;

    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date updatedAt;


    public IncidentRequest(String userName, String pickUpLocation, String dropOffLocation, String phoneNumber, 
                           String incident, String additionalInformation, String incidentStatus, String backofficeAmbulanceId) {
        this.userName = userName;
        this.pickUpLocation = pickUpLocation;
        this.dropOffLocation = dropOffLocation;
        this.phoneNumber = phoneNumber;
        this.incident = incident;
        this.additionalInformation = additionalInformation;
        this.incidentStatus = incidentStatus;
        this.backofficeAmbulanceId = backofficeAmbulanceId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public IncidentRequest() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getpickUpLocation() {
        return pickUpLocation;
    }

    public void setpickUpLocation(String pickUpLocation) {
        this.pickUpLocation = pickUpLocation;
    }

    public String getdropOffLocation() {
        return dropOffLocation;
    }

    public void setdropOffLocation(String dropOffLocation) {
        this.dropOffLocation = dropOffLocation;
    }

    public String getIncident() {
        return incident;
    }

    public void setIncident(String incident) {
        this.incident = incident;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getIncidentStatus() {
        return incidentStatus;
    }

    public void setIncidentStatus(String incidentStatus) {
        this.incidentStatus = incidentStatus;
    }

    public String getBackofficeAmbulanceId() {
        return backofficeAmbulanceId;
    }

    public void setBackofficeAmbulanceId(String backofficeAmbulanceId) {
        this.backofficeAmbulanceId = backofficeAmbulanceId;
    }
}
