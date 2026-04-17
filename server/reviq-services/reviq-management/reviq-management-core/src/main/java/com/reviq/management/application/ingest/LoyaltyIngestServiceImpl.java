package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.LoyaltyIngestService;
import com.reviq.management.api.ingest.dto.CardTierHistoryRequest;
import com.reviq.management.api.ingest.dto.LoyaltyCardRequest;
import com.reviq.management.api.ingest.dto.LoyaltyTierRequest;
import com.reviq.management.domain.loyalty.entity.CardTierHistory;
import com.reviq.management.domain.loyalty.entity.LoyaltyCard;
import com.reviq.management.domain.loyalty.entity.LoyaltyTier;
import com.reviq.management.domain.loyalty.repository.LoyaltyCardRepository;
import com.reviq.management.domain.loyalty.repository.LoyaltyTierRepository;
import com.reviq.management.domain.partner.repository.ContactRepository;
import com.reviq.shared.enums.CardStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoyaltyIngestServiceImpl implements LoyaltyIngestService {

    private final LoyaltyTierRepository loyaltyTierRepository;
    private final LoyaltyCardRepository loyaltyCardRepository;
    private final ContactRepository contactRepository;

    @Override
    @Transactional
    public UUID upsertTier(LoyaltyTierRequest request) {
        LoyaltyTier tier = loyaltyTierRepository.findByExternalId(request.getExternalId())
                .orElseGet(LoyaltyTier::new);

        tier.setExternalId(request.getExternalId());
        tier.setCode(request.getCode());
        tier.setName(request.getName());
        tier.setDiscount(request.getDiscount());
        tier.setValidFrom(request.getValidFrom());
        tier.setValidTo(request.getValidTo());
        tier.setThresholdAmount(request.getThresholdAmount());
        tier.setLoyaltyProgram(request.getLoyaltyProgram());
        tier.setSyncedAt(LocalDateTime.now());

        if (request.getPointsBased() != null) {
            tier.setPointsBased(request.getPointsBased());
        }

        return loyaltyTierRepository.save(tier).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertTierBatch(List<LoyaltyTierRequest> requests) {
        return requests.stream().map(this::upsertTier).toList();
    }

    @Override
    @Transactional
    public UUID upsertCard(LoyaltyCardRequest request) {
        LoyaltyCard card = loyaltyCardRepository.findByExternalId(request.getExternalId())
                .orElseGet(LoyaltyCard::new);

        card.setExternalId(request.getExternalId());
        card.setBarcode(request.getBarcode());
        card.setBalance(request.getBalance());
        card.setSyncedAt(LocalDateTime.now());

        if (request.getStatus() != null) {
            card.setStatus(CardStatus.valueOf(request.getStatus()));
        }
        if (request.getContactExternalId() != null) {
            contactRepository.findByExternalId(request.getContactExternalId())
                    .ifPresent(card::setContact);
        }
        if (request.getCurrentTierExternalId() != null) {
            loyaltyTierRepository.findByExternalId(request.getCurrentTierExternalId())
                    .ifPresent(card::setCurrentTier);
        }

        // Replace tier history
        card.getTierHistory().clear();
        if (request.getTierHistory() != null) {
            for (CardTierHistoryRequest histReq : request.getTierHistory()) {
                loyaltyTierRepository.findByExternalId(histReq.getTierExternalId())
                        .ifPresent(tier -> {
                            CardTierHistory history = CardTierHistory.builder()
                                    .card(card)
                                    .tier(tier)
                                    .validFrom(histReq.getValidFrom())
                                    .validTo(histReq.getValidTo())
                                    .build();
                            card.getTierHistory().add(history);
                        });
            }
        }

        return loyaltyCardRepository.save(card).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertCardBatch(List<LoyaltyCardRequest> requests) {
        return requests.stream().map(this::upsertCard).toList();
    }
}
