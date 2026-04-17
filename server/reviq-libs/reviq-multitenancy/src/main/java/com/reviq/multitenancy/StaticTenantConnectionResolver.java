package com.reviq.multitenancy;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.net.URI;

@RequiredArgsConstructor
public class StaticTenantConnectionResolver implements TenantConnectionResolver {

    private final DataSourceProperties dataSourceProperties;

    @Override
    public ConnectionInfo resolve(String tenantId) {
        String url = dataSourceProperties.getUrl();
        String host = "localhost";
        int port = 5432;
        String dbName = "reviq_db";

        if (url != null && url.startsWith("jdbc:postgresql://")) {
            String stripped = url.substring("jdbc:postgresql://".length());
            URI uri = URI.create("pg://" + stripped);
            host = uri.getHost();
            port = uri.getPort() > 0 ? uri.getPort() : 5432;
            dbName = uri.getPath().substring(1);
        }

        return new ConnectionInfo(
                "default",
                host,
                port,
                dbName,
                dataSourceProperties.getUsername(),
                dataSourceProperties.getPassword(),
                null
        );
    }
}
