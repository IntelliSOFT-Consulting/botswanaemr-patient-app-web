package com.intellisoft.botswanaemrappointments.service.models.response.slots;

import lombok.*;

import java.util.Date;
import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AppointmentBlock{
    private String uuid;
    private String display;
    private Date startDate;
    private Date endDate;
    private Provider provider;
    private Location location;
    private List<Type> types;
    private boolean voided;
    private List<Link> links;
    private String resourceVersion;
}
