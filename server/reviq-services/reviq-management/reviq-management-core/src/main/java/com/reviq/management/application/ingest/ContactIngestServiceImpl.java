package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.ContactIngestService;
import com.reviq.management.api.ingest.dto.ContactRequest;
import com.reviq.management.domain.partner.entity.Contact;
import com.reviq.management.domain.partner.repository.ContactRepository;
import com.reviq.shared.enums.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactIngestServiceImpl implements ContactIngestService {

    private final ContactRepository contactRepository;

    @Override
    @Transactional
    public UUID upsert(ContactRequest request) {
        Contact contact = contactRepository.findByExternalId(request.getExternalId())
                .orElseGet(Contact::new);

        contact.setExternalId(request.getExternalId());
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setStreet(request.getStreet());
        contact.setCity(request.getCity());
        contact.setPostalCode(request.getPostalCode());
        contact.setPhone(request.getPhone());
        contact.setMobile(request.getMobile());
        contact.setEmail(request.getEmail());
        contact.setSyncedAt(LocalDateTime.now());

        if (request.getGender() != null) {
            contact.setGender(Gender.valueOf(request.getGender()));
        }
        if (request.getDateOfBirth() != null) {
            contact.setDateOfBirth(request.getDateOfBirth());
        }

        return contactRepository.save(contact).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertBatch(List<ContactRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }
}
