package com.reviq.multitenancy.datasource;

import com.reviq.multitenancy.ConnectionInfo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class TenantDataSourceCache {

    private final ConcurrentMap<String, DataSource> cache = new ConcurrentHashMap<>();

    public DataSource getOrCreate(String tenantId, ConnectionInfo info) {
        return cache.computeIfAbsent(tenantId, id -> {
            log.info("Creating DataSource for tenant: {}", id);
            return createDataSource(id, info);
        });
    }

    public void remove(String tenantId) {
        DataSource ds = cache.remove(tenantId);
        if (ds instanceof HikariDataSource hikari) {
            log.info("Closing DataSource for tenant: {}", tenantId);
            hikari.close();
        }
    }

    private DataSource createDataSource(String tenantId, ConnectionInfo info) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(info.jdbcUrl());
        config.setUsername(info.dbUsername());
        config.setPassword(info.dbPassword());
        if (info.schemaName() != null) {
            config.setSchema(info.schemaName());
        }
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setPoolName("tenant-" + tenantId);
        config.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(config);
    }
}
