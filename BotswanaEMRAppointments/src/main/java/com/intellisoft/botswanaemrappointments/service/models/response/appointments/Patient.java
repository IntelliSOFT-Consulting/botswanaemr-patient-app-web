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
public class Patient {
    private String uuid;
    private String display;
    private List<Identifier> identifiers;
    private Person person;
    private boolean voided;
    private List<Link> links;
    private String resourceVersion;
}
