package com.reviq.tenancy.infrastructure.listener;

import com.reviq.tenancy.api.event.TenantEventConstants;
import com.reviq.tenancy.api.event.TenantProvisioningCompletedEvent;
import com.reviq.tenancy.api.event.TenantProvisioningResultEvent;
import com.reviq.tenancy.domain.entity.TenantDatabase;
import com.reviq.tenancy.shared.enums.TenantStatus;
import com.reviq.tenancy.domain.repository.TenantDatabaseRepository;
import com.reviq.tenancy.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantProvisioningResultListener {

    private final TenantRepository tenantRepository;
    private final TenantDatabaseRepository tenantDatabaseRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = TenantEventConstants.QUEUE_PROVISIONING_RESULTS)
    @Transactional
    public void onProvisioningResult(TenantProvisioningResultEvent event) {
        log.info("Received provisioning result for tenant '{}' from service '{}': success={}",
                event.code(), event.serviceName(), event.success());

        var tenant = tenantRepository.findById(event.tenantId())
                .orElseThrow(() -> new IllegalStateException("Tenant not found: " + event.tenantId()));

        TenantDatabase db = tenant.getDatabase();

        if (!event.success()) {
            log.error("Provisioning failed for tenant '{}' in service '{}': {}",
                    event.code(), event.serviceName(), event.errorMessage());
            return;
        }

        switch (event.serviceName()) {
            case "management" -> db.setManagementSchemaProvisioned(true);
            case "ai" -> db.setAiSchemaProvisioned(true);
            default -> log.warn("Unknown service name: {}", event.serviceName());
        }

        tenantDatabaseRepository.save(db);

        if (Boolean.TRUE.equals(db.getManagementSchemaProvisioned())
                && Boolean.TRUE.equals(db.getAiSchemaProvisioned())) {

            tenant.setStatus(TenantStatus.ACTIVE);
            tenantRepository.save(tenant);

            log.info("Tenant '{}' fully provisioned — status set to ACTIVE", event.code());

            rabbitTemplate.convertAndSend(
                    TenantEventConstants.EXCHANGE,
                    TenantEventConstants.ROUTING_PROVISIONING_COMPLETED,
                    new TenantProvisioningCompletedEvent(tenant.getId(), tenant.getCode())
            );
        }
    }
}
