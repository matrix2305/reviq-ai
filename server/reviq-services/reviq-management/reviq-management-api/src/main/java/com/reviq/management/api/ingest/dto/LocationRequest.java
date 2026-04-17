package com.reviq.management.api.ingest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {
    private String externalId;
    private String code;
    private String name;
    private String type;
    private String parentExternalId;
    private String street;
    private String city;
    private String zone;
    private Boolean active;
}
