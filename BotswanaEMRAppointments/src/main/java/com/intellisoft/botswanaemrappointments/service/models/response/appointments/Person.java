package com.intellisoft.botswanaemrappointments.service.models.response.appointments;

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
public class Person {
    private String uuid;
    private String display;
    private String gender;
    private int age;
    private Date birthdate;
    private boolean birthdateEstimated;
    private boolean dead;
    private Date deathDate;
    private String causeOfDeath;
    private PreferredName preferredName;
    private PreferredAddress preferredAddress;
    private List<Object> attributes;
    private boolean voided;
    private Object birthtime;
    private boolean deathdateEstimated;
    private List<Link> links;
    private String resourceVersion;
}
