package com.reviq.tenancy.application.service;

import com.reviq.tenancy.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TenantCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

    private final TenantRepository tenantRepository;

    public String generate(String displayName) {
        String base = slugify(displayName);
        if (base.length() > 20) {
            base = base.substring(0, 20);
        }

        String code = base;
        if (!tenantRepository.existsByCode(code)) {
            return code;
        }

        // Append random suffix until unique
        for (int i = 0; i < 10; i++) {
            code = base + "_" + randomSuffix(4);
            if (!tenantRepository.existsByCode(code)) {
                return code;
            }
        }

        throw new IllegalStateException("Could not generate unique tenant code for: " + displayName);
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
    }

    private String randomSuffix(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
