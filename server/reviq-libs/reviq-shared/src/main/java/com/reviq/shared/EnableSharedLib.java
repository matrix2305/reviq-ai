package com.reviq.shared;

import com.reviq.shared.config.OpenApiConfig;
import com.reviq.shared.context.RequestContextAmqpConfig;
import com.reviq.shared.exception.GlobalExceptionHandler;
import com.reviq.shared.seeder.SeederCoordinator;
import com.reviq.shared.seeder.SeederProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({SeederCoordinator.class, GlobalExceptionHandler.class, OpenApiConfig.class, RequestContextAmqpConfig.class})
@EnableConfigurationProperties(SeederProperties.class)
public @interface EnableSharedLib {
}
