package com.intellisoft.botswanaemrappointments.service.models.response.appointments;

import com.intellisoft.botswanaemrappointments.service.models.response.slots.Link;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestedBy {
    private String uuid;
    private String display;
    private Person person;
    private String identifier;
    private List<Object> attributes;
    private boolean retired;
    private List<Link> links;
    private String resourceVersion;
}
