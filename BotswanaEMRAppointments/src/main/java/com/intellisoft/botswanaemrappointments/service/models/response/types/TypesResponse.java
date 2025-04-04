package com.intellisoft.botswanaemrappointments.service.models.response.types;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TypesResponse {
    private List<TypeResult> results;
}
