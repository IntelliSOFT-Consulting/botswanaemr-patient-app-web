package com.intellisoft.botswanaemrappointments.service.models.response.slots;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SlotsResponse {
    List<Slot> results;
}
