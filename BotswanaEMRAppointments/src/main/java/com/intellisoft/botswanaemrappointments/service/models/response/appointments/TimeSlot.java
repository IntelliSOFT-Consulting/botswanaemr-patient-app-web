package com.intellisoft.botswanaemrappointments.service.models.response.appointments;

import com.intellisoft.botswanaemrappointments.service.models.response.slots.AppointmentBlock;
import com.intellisoft.botswanaemrappointments.service.models.response.slots.Link;
import lombok.*;

import java.util.Date;
import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TimeSlot {
    private String uuid;
    private String display;
    private Date startDate;
    private Date endDate;
    private AppointmentBlock appointmentBlock;
    private int countOfAppointments;
    private int unallocatedMinutes;
    private boolean voided;
    private List<Link> links;
    private String resourceVersion;
}
