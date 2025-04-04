package com.intellisoft.botswanaemrappointments.service.models.response.slots;

import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Provider {
    private String uuid;
    private String display;
    private Person person;
}
