package com.reviq.shared.seeder;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "data-seeder")
public class SeederProperties {

    private boolean run = false;
    private String lockName = "default";
}
