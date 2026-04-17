package com.reviq.tenancy.application.service;

import com.reviq.tenancy.api.TenantManagementService;
import com.reviq.tenancy.api.dto.CreateTenantRequest;
import com.reviq.tenancy.api.dto.CreateTenantResponse;
import com.reviq.tenancy.api.dto.TenantDto;
import com.reviq.tenancy.api.event.TenantEventConstants;
import com.reviq.tenancy.api.event.TenantProvisioningRequestedEvent;
import com.reviq.tenancy.application.mapper.TenantMapper;
import com.reviq.tenancy.domain.entity.SubscriptionPlan;
import com.reviq.tenancy.domain.entity.Tenant;
import com.reviq.tenancy.domain.entity.TenantDatabase;
import com.reviq.tenancy.domain.entity.TenantSubscription;
import com.reviq.tenancy.domain.repository.SubscriptionPlanRepository;
import com.reviq.tenancy.domain.repository.TenantRepository;
import com.reviq.tenancy.domain.repository.TenantSubscriptionRepository;
import com.reviq.tenancy.infrastructure.config.TenantDatabaseProperties;
import com.reviq.tenancy.shared.enums.BillingCycle;
import com.reviq.tenancy.shared.enums.SubscriptionStatus;
import com.reviq.tenancy.shared.enums.TenantStatus;
import com.reviq.tenancy.shared.enums.TenantType;
import com.reviq.identity.grpc.UserIdentityServiceGrpc;
import com.reviq.identity.grpc.CreateUserGrpcRequest;
import com.reviq.shared.exception.BadRequestException;
import com.reviq.shared.exception.NotFoundException;
import com.reviq.shared.search.SearchRequest;
import com.reviq.shared.search.SearchSpecification;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class TenantManagementServiceImpl implements TenantManagementService {

    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository planRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final TenantMapper tenantMapper;
    private final TenantCodeGenerator codeGenerator;
    private final TenantDatabaseProperties dbProperties;
    private final PaddleCheckoutService checkoutService;
    private final RabbitTemplate rabbitTemplate;

    @GrpcClient("identity-service")
    private UserIdentityServiceGrpc.UserIdentityServiceBlockingStub identityStub;

    public TenantManagementServiceImpl(TenantRepository tenantRepository,
                                       SubscriptionPlanRepository planRepository,
                                       TenantSubscriptionRepository subscriptionRepository,
                                       TenantMapper tenantMapper,
                                       TenantCodeGenerator codeGenerator,
                                       TenantDatabaseProperties dbProperties,
                                       PaddleCheckoutService checkoutService,
                                       RabbitTemplate rabbitTemplate) {
        this.tenantRepository = tenantRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.tenantMapper = tenantMapper;
        this.codeGenerator = codeGenerator;
        this.dbProperties = dbProperties;
        this.checkoutService = checkoutService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public CreateTenantResponse createTenant(CreateTenantRequest request) {
        String code = codeGenerator.generate(request.getOrganizationName());
        log.info("Creating tenant '{}' with code '{}'", request.getOrganizationName(), code);

        Tenant tenant = Tenant.builder()
                .code(code)
                .organizationName(request.getOrganizationName())
                .status(TenantStatus.PROVISIONING)
                .type(request.getType() != null ? request.getType() : TenantType.RETAIL)
                .build();

        String dbName = dbProperties.getNamePrefix() + code;
        TenantDatabase database = TenantDatabase.builder()
                .tenant(tenant)
                .dbHost(dbProperties.getHost())
                .dbPort(dbProperties.getPort())
                .dbName(dbName)
                .dbUsername(dbProperties.getUsername())
                .dbPassword(dbProperties.getPassword())
                .build();
        tenant.setDatabase(database);

        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new NotFoundException("PLAN_NOT_FOUND", "Plan not found: " + request.getPlanId()));

        BillingCycle billingCycle = BillingCycle.valueOf(request.getBillingCycle().toUpperCase());

        TenantSubscription subscription = TenantSubscription.builder()
                .tenant(tenant)
                .plan(plan)
                .status(SubscriptionStatus.PENDING)
                .billingCycle(billingCycle)
                .build();
        tenant.setSubscription(subscription);

        tenant = tenantRepository.save(tenant);

        createAdminUser(tenant, request);
        publishProvisioningEvent(tenant);

        var checkout = checkoutService.createCheckout(tenant.getId(), plan.getName(), billingCycle);

        log.info("Tenant '{}' created with admin user '{}' and checkout URL",
                code, request.getAdminEmail());

        return CreateTenantResponse.builder()
                .tenant(tenantMapper.toDto(tenant))
                .checkoutUrl(checkout.checkoutUrl())
                .build();
    }

    @Override
    public TenantDto provisionTenant(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + tenantId));
        publishProvisioningEvent(tenant);
        return tenantMapper.toDto(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TenantDto> search(SearchRequest request) {
        SearchSpecification<Tenant> spec = new SearchSpecification<>(Tenant.class, request);
        Pageable pageable = SearchSpecification.getPageable(request.getPage(), request.getSize());
        return tenantRepository.findAll(spec, pageable).map(tenantMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantDto findById(UUID id) {
        return tenantRepository.findById(id)
                .map(tenantMapper::toDto)
                .orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public TenantDto findByCode(String code) {
        return tenantRepository.findByCode(code)
                .map(tenantMapper::toDto)
                .orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + code));
    }

    @Override
    public TenantDto decommissionTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TENANT_NOT_FOUND", "Tenant not found: " + id));
        tenant.setStatus(TenantStatus.DECOMMISSIONED);
        tenantRepository.save(tenant);
        log.info("Tenant decommissioned: {}", tenant.getCode());
        return tenantMapper.toDto(tenant);
    }

    private void createAdminUser(Tenant tenant, CreateTenantRequest request) {
        try {
            var response = identityStub.createUser(
                    CreateUserGrpcRequest.newBuilder()
                            .setTenantCode(tenant.getCode())
                            .setTenantId(tenant.getId().toString())
                            .setEmail(request.getAdminEmail())
                            .setPassword(request.getAdminPassword())
                            .setFirstName(request.getAdminFirstName())
                            .setLastName(request.getAdminLastName())
                            .setRoleCode("ORGANIZATION_ADMIN")
                            .build()
            );
            log.info("Admin user created via gRPC: {} (id: {}) for tenant '{}'",
                    response.getEmail(), response.getId(), tenant.getCode());
        } catch (Exception e) {
            log.error("Failed to create admin user for tenant '{}': {}", tenant.getCode(), e.getMessage(), e);
            throw new BadRequestException("ADMIN_CREATION_FAILED", "Failed to create admin user: " + e.getMessage());
        }
    }

    private void publishProvisioningEvent(Tenant tenant) {
        rabbitTemplate.convertAndSend(
                TenantEventConstants.EXCHANGE,
                TenantEventConstants.ROUTING_PROVISIONING_REQUESTED,
                new TenantProvisioningRequestedEvent(tenant.getId(), tenant.getCode())
        );
        log.info("Published provisioning event for tenant: {}", tenant.getCode());
    }
}
