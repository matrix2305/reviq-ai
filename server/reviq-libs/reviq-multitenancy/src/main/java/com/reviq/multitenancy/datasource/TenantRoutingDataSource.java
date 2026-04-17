package com.reviq.multitenancy.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final DataSource defaultDataSource;

    public TenantRoutingDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
        setDefaultTargetDataSource(defaultDataSource);
        setTargetDataSources(java.util.Map.of());
        afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return null;
    }

    @Override
    protected DataSource determineTargetDataSource() {
        DataSource tenantDs = TenantDataSourceHolder.get();
        return tenantDs != null ? tenantDs : defaultDataSource;
    }
}
