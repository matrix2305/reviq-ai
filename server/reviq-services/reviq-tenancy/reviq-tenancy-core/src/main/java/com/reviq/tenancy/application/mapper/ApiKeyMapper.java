package com.reviq.tenancy.application.mapper;

import com.reviq.tenancy.api.dto.ApiKeyDto;
import com.reviq.tenancy.domain.entity.TenantApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApiKeyMapper {

    @Mapping(source = "tenant.code", target = "tenantCode")
    ApiKeyDto toDto(TenantApiKey entity);

    List<ApiKeyDto> toDtoList(List<TenantApiKey> entities);
}
