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
public class AppointmentType {
    public String uuid;
    public String display;
    public List<Link> links;
}
