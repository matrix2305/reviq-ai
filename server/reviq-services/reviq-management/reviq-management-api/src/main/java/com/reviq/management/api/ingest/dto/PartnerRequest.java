package com.reviq.management.api.ingest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRequest {
    private String externalId;
    private String type;
    private String name;
    private String taxNumber;
    private String registrationNumber;
    private Boolean active;
    private String address;
    private String city;
    private String postalCode;
    private String phone;
    private String fax;
}
