package com.reviq.management.sync.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ErpDataSourceConfig {

    @Bean
    @ConfigurationProperties("erp.datasource")
    public DataSourceProperties erpDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "erpDataSource")
    public DataSource erpDataSource() {
        return erpDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "erpJdbcTemplate")
    public JdbcTemplate erpJdbcTemplate(@Qualifier("erpDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
