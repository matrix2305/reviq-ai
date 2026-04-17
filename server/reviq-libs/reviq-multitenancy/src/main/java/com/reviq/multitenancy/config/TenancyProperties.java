package com.reviq.multitenancy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "reviq.tenancy")
public class TenancyProperties {

    private TenancyMode mode = TenancyMode.SINGLE;

    private String schemaName = "public";

    private Grpc grpc = new Grpc();

    @Getter
    @Setter
    public static class Grpc {
        private String host = "localhost";
        private int port = 9092;
    }
}
