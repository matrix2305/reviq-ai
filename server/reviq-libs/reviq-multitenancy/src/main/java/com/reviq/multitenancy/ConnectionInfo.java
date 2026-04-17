package com.reviq.multitenancy;

public record ConnectionInfo(
        String tenantId,
        String dbHost,
        Integer dbPort,
        String dbName,
        String dbUsername,
        String dbPassword,
        String schemaName
) {
    public String jdbcUrl() {
        return "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
    }
}
