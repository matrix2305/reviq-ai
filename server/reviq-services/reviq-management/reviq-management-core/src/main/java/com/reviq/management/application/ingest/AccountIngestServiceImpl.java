package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.AccountIngestService;
import com.reviq.management.api.ingest.dto.AccountRequest;
import com.reviq.management.domain.account.entity.Account;
import com.reviq.management.domain.account.repository.AccountRepository;
import com.reviq.management.domain.location.repository.LocationRepository;
import com.reviq.management.domain.partner.repository.PartnerRepository;
import com.reviq.management.domain.product.repository.ProductRepository;
import com.reviq.shared.enums.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountIngestServiceImpl implements AccountIngestService {

    private final AccountRepository accountRepository;
    private final PartnerRepository partnerRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public UUID upsert(AccountRequest request) {
        Account account = accountRepository.findByExternalId(request.getExternalId())
                .orElseGet(Account::new);

        account.setExternalId(request.getExternalId());
        account.setAccountNumber(request.getAccountNumber());
        account.setType(AccountType.valueOf(request.getType()));
        account.setCurrency(request.getCurrency());
        account.setSyncedAt(LocalDateTime.now());

        if (request.getBalance() != null) {
            account.setBalance(request.getBalance());
        }
        if (request.getActive() != null) {
            account.setActive(request.getActive());
        }
        if (request.getPartnerExternalId() != null) {
            partnerRepository.findByExternalId(request.getPartnerExternalId())
                    .ifPresent(account::setPartner);
        }
        if (request.getLocationExternalId() != null) {
            locationRepository.findByExternalId(request.getLocationExternalId())
                    .ifPresent(account::setLocation);
        }
        if (request.getProductExternalId() != null) {
            productRepository.findByExternalId(request.getProductExternalId())
                    .ifPresent(account::setProduct);
        }

        return accountRepository.save(account).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertBatch(List<AccountRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }
}
