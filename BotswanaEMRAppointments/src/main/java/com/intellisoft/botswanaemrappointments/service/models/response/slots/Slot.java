package com.intellisoft.botswanaemrappointments.service.models.response.slots;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Slot {
    private String uuid;
    private String display;
    private Date startDate;
    private Date endDate;
    private AppointmentBlock appointmentBlock;
    private int countOfAppointments;
    private int unallocatedMinutes;
    private boolean voided;
}