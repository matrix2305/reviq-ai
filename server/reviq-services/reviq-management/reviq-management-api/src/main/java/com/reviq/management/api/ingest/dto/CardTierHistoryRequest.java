package com.reviq.management.api.ingest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTierHistoryRequest {
    private String tierExternalId;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}
