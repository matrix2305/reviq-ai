package com.reviq.multitenancy.config;

import com.reviq.multitenancy.GrpcTenantConnectionResolver;
import com.reviq.multitenancy.StaticTenantConnectionResolver;
import com.reviq.multitenancy.TenantConnectionResolver;
import com.reviq.multitenancy.TenantInterceptor;
import com.reviq.multitenancy.amqp.TenantDataSourceAdviceContributor;
import com.reviq.multitenancy.datasource.TenantDataSourceCache;
import com.reviq.multitenancy.datasource.TenantRoutingDataSource;
import com.reviq.multitenancy.grpc.TenantGrpcClientInterceptor;
import com.reviq.multitenancy.grpc.TenantGrpcServerInterceptor;
import com.reviq.multitenancy.provisioning.SchemaProvisioner;
import com.reviq.multitenancy.provisioning.TenantLiquibaseMigrationRunner;
import com.reviq.tenancy.grpc.TenancyServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(TenancyProperties.class)
public class TenantClientAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "reviq.tenancy.mode", havingValue = "single", matchIfMissing = true)
    public TenantConnectionResolver staticTenantConnectionResolver(DataSourceProperties props) {
        return new StaticTenantConnectionResolver(props);
    }

    @Bean
    @ConditionalOnProperty(name = "reviq.tenancy.mode", havingValue = "multi")
    public TenantConnectionResolver grpcTenantConnectionResolver(
            TenancyProperties properties,
            @GrpcClient("tenancy-service") TenancyServiceGrpc.TenancyServiceBlockingStub tenancyStub) {
        return new GrpcTenantConnectionResolver(properties, tenancyStub);
    }

    @Bean
    @ConditionalOnProperty(name = "reviq.tenancy.mode", havingValue = "multi")
    public TenantDataSourceCache tenantDataSourceCache() {
        return new TenantDataSourceCache();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "reviq.tenancy.mode", havingValue = "multi")
    public DataSource tenantRoutingDataSource(DataSource defaultDataSource) {
        return new TenantRoutingDataSource(defaultDataSource);
    }

    @Bean
    @ConditionalOnProperty(name = "reviq.tenancy.mode", havingValue = "multi")
    public WebMvcConfigurer tenantWebMvcConfigurer(TenantConnectionResolver resolver,
                                                    TenantDataSourceCache cache) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new TenantInterceptor(resolver, cache))
                        .addPathPatterns("/api/v1/**")
                        .excludePathPatterns("/api/v1/admin/**");
            }
        };
    }

    // Jackson message converter for RabbitMQ event serialization
    @Bean
    @ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // gRPC interceptors

    @Bean
    @ConditionalOnProperty(name = "reviq.tenancy.mode", havingValue = "multi")
    public TenantGrpcClientInterceptor tenantGrpcClientInterceptor() {
        return new TenantGrpcClientInterceptor();
    }

    @Bean
    @ConditionalOnProperty(name = "reviq.tenancy.mode", havingValue = "multi")
    public TenantGrpcServerInterceptor tenantGrpcServerInterceptor() {
        return new TenantGrpcServerInterceptor();
    }

    // RabbitMQ: resolve tenant DataSource for message listeners
    @Bean
    @ConditionalOnProperty(name = "reviq.tenancy.mode", havingValue = "multi")
    public TenantDataSourceAdviceContributor tenantDataSourceAdviceContributor(
            TenantConnectionResolver resolver, TenantDataSourceCache cache) {
        return new TenantDataSourceAdviceContributor(resolver, cache);
    }

    // Multi-tenant Liquibase: on startup, migrate all active tenants
    @Bean
    @ConditionalOnProperty(name = "reviq.tenancy.mode", havingValue = "multi")
    @ConditionalOnBean(SchemaProvisioner.class)
    public TenantLiquibaseMigrationRunner tenantLiquibaseMigrationRunner(
            SchemaProvisioner schemaProvisioner,
            @GrpcClient("tenancy-service") TenancyServiceGrpc.TenancyServiceBlockingStub tenancyStub) {
        return new TenantLiquibaseMigrationRunner(schemaProvisioner, tenancyStub);
    }
}
