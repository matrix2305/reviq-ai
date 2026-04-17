package com.reviq.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "reviq.gateway")
public class GatewayProperties {

    private ServiceUrls services = new ServiceUrls();

    @Getter
    @Setter
    public static class ServiceUrls {
        private String userIdentity = "http://localhost:8083";
        private String tenancy = "http://localhost:8082";
        private String management = "http://localhost:8080";
        private String ai = "http://localhost:8081";
    }
}
