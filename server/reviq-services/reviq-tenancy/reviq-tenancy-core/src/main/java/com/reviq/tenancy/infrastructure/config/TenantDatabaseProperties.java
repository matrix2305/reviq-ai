package com.reviq.tenancy.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reviq.tenant-database")
public class TenantDatabaseProperties {

    private String host = "localhost";
    private int port = 5432;
    private String username = "reviq";
    private String password = "reviq123";
    private String namePrefix = "reviq_tenant_";
}
