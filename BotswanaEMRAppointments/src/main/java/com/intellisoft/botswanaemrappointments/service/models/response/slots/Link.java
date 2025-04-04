package com.intellisoft.botswanaemrappointments.service.models.response.slots;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Link{
    private String rel;
    private String uri;
    private String resourceAlias;
}
