package com.intellisoft.botswanaemrappointments.service.models.response.appointments;

import com.intellisoft.botswanaemrappointments.service.models.response.slots.Link;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestAppResponse {
    private String uuid;
    private String display;
    private Patient patient;
    private AppointmentType appointmentType;
    private Object provider;
    private RequestedBy requestedBy;
    private String requestedOn;
    private String status;
    private int minTimeFrameValue;
    private String minTimeFrameUnits;
    private int maxTimeFrameValue;
    private String maxTimeFrameUnits;
    private String notes;
    private boolean voided;
    private List<Link> links;
    private String resourceVersion;

}