package com.reviq.tenancy.application.mapper;

import com.reviq.tenancy.api.dto.TenantDto;
import com.reviq.tenancy.domain.entity.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    @Mapping(source = "subscription.plan.displayName", target = "subscriptionPlan")
    @Mapping(source = "subscription.status", target = "subscriptionStatus")
    TenantDto toDto(Tenant entity);

    List<TenantDto> toDtoList(List<Tenant> entities);
}
