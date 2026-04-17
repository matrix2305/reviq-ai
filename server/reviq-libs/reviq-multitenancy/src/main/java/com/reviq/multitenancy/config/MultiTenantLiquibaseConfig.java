package com.reviq.multitenancy.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * In multi-tenant mode, disables Spring Boot's default Liquibase auto-run.
 * Migrations are handled per-tenant by TenantLiquibaseMigrationRunner on startup.
 *
 * In single-tenant mode, this config is not loaded — Spring Boot's default Liquibase works normally.
 */
@Configuration
@ConditionalOnProperty(name = "reviq.tenancy.mode", havingValue = "multi")
public class MultiTenantLiquibaseConfig {

    @Bean
    @Primary
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setShouldRun(false);
        return liquibase;
    }
}
