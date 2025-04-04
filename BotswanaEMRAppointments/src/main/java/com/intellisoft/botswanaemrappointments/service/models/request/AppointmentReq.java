package com.intellisoft.botswanaemrappointments.service.models.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AppointmentReq {
    private String appointmentType;
    private String patient;
    private String reason;
    private String status;
    private String timeSlot;
}
