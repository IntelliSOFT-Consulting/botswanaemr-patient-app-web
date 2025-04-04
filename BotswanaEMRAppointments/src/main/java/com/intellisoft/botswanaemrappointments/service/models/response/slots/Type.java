package com.intellisoft.botswanaemrappointments.service.models.response.slots;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Type{
    private String uuid;
    private String display;
    private List<Link> links;
}
