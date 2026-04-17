package com.reviq.multitenancy.provisioning;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;

@Slf4j
public abstract class AbstractSchemaProvisioner implements SchemaProvisioner {

    @Override
    public void provisionSchema(String dbUrl, String dbUsername, String dbPassword) {
        log.info("Provisioning schema '{}' in database: {}", getSchemaName(), dbUrl);

        createDatabaseIfNotExists(dbUrl, dbUsername, dbPassword);
        createSchemaAndMigrate(dbUrl, dbUsername, dbPassword);

        log.info("Schema '{}' provisioned successfully", getSchemaName());
    }

    private void createDatabaseIfNotExists(String dbUrl, String dbUsername, String dbPassword) {
        String baseUrl = dbUrl.substring(0, dbUrl.lastIndexOf('/'));
        String dbName = dbUrl.substring(dbUrl.lastIndexOf('/') + 1);
        validateIdentifier(dbName);

        try (Connection conn = DriverManager.getConnection(baseUrl + "/postgres", dbUsername, dbPassword)) {
            var ps = conn.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?");
            ps.setString(1, dbName);
            var rs = ps.executeQuery();
            if (!rs.next()) {
                String escaped = "\"" + dbName.replace("\"", "\"\"") + "\"";
                conn.createStatement().execute("CREATE DATABASE " + escaped);
                log.info("Created database: {}", dbName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create database: " + dbName, e);
        }
    }

    private void createSchemaAndMigrate(String dbUrl, String dbUsername, String dbPassword) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setMaximumPoolSize(2);
        config.setPoolName("provisioning-" + getSchemaName());

        try (HikariDataSource ds = new HikariDataSource(config)) {
            try (Connection conn = ds.getConnection()) {
                String escaped = "\"" + getSchemaName().replace("\"", "\"\"") + "\"";
                conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS " + escaped);
                log.info("Schema '{}' ensured", getSchemaName());
            }

            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDataSource(ds);
            liquibase.setChangeLog(getChangelogPath());
            liquibase.setDefaultSchema(getSchemaName());
            liquibase.setLiquibaseSchema(getSchemaName());
            liquibase.setShouldRun(true);
            liquibase.afterPropertiesSet();

            log.info("Liquibase migrations completed for schema '{}'", getSchemaName());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to provision schema '" + getSchemaName() + "': " + e.getMessage(), e);
        }
    }

    private void validateIdentifier(String name) {
        if (!name.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid database identifier: " + name);
        }
    }
}
