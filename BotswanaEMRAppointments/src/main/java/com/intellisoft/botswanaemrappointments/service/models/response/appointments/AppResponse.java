package com.intellisoft.botswanaemrappointments.service.models.response.appointments;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AppResponse {
    private List<AppResult> results;
}
