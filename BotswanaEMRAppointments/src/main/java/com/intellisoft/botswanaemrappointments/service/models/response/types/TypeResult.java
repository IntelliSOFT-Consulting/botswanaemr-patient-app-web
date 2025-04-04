package com.intellisoft.botswanaemrappointments.service.models.response.types;

import com.intellisoft.botswanaemrappointments.service.models.response.slots.Link;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TypeResult {
    public String uuid;
    public String display;
    public String name;
    public Object description;
    public int duration;
    public boolean confidential;
    public Object visitType;
    public boolean retired;
    public List<Link> links;
    public String resourceVersion;
}
