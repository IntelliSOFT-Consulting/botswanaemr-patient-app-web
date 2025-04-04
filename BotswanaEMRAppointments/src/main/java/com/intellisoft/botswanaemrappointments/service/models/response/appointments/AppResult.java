package com.intellisoft.botswanaemrappointments.service.models.response.appointments;

import com.intellisoft.botswanaemrappointments.service.models.response.slots.Link;
import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AppResult {
    private String uuid;
    private String display;
    private TimeSlot timeSlot;
    private Object visit;
    private Patient patient;
    private String status;
    private String reason;
    private Object cancelReason;
    private AppointmentType appointmentType;
    private boolean voided;
    private List<Link> links;
    private String resourceVersion;
}
