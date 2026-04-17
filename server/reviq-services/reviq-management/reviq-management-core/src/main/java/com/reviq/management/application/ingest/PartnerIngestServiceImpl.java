package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.PartnerIngestService;
import com.reviq.management.api.ingest.dto.PartnerRequest;
import com.reviq.management.domain.partner.entity.Partner;
import com.reviq.management.domain.partner.repository.PartnerRepository;
import com.reviq.shared.enums.PartnerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PartnerIngestServiceImpl implements PartnerIngestService {

    private final PartnerRepository partnerRepository;

    @Override
    @Transactional
    public UUID upsert(PartnerRequest request) {
        Partner partner = partnerRepository.findByExternalId(request.getExternalId())
                .orElseGet(Partner::new);

        partner.setExternalId(request.getExternalId());
        partner.setName(request.getName());
        partner.setTaxNumber(request.getTaxNumber());
        partner.setRegistrationNumber(request.getRegistrationNumber());
        partner.setAddress(request.getAddress());
        partner.setCity(request.getCity());
        partner.setPostalCode(request.getPostalCode());
        partner.setPhone(request.getPhone());
        partner.setFax(request.getFax());
        partner.setSyncedAt(LocalDateTime.now());

        if (request.getType() != null) {
            partner.setType(PartnerType.valueOf(request.getType()));
        }
        if (request.getActive() != null) {
            partner.setActive(request.getActive());
        }

        return partnerRepository.save(partner).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertBatch(List<PartnerRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }
}
