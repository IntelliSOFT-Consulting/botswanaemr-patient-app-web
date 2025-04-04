package com.intellisoft.botswanaemrappointments.service.models.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestAppointment {
    private String status="PENDING";
    private String requestedOn;
    private String patient;
    private String requestedBy;
    private String appointmentType;
    private String provider;
    private String minTimeFrameValue=null;
    private String minTimeFrameUnits=null;
    private String maxTimeFrameValue=null;
    private String maxTimeFrameUnits=null;
    private String notes;
}
