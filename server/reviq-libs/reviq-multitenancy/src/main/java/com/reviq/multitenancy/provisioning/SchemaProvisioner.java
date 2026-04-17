package com.reviq.multitenancy.provisioning;

public interface SchemaProvisioner {

    void provisionSchema(String dbUrl, String dbUsername, String dbPassword);

    String getSchemaName();

    String getChangelogPath();
}
