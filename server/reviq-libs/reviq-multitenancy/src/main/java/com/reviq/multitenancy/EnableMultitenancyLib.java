package com.reviq.multitenancy;

import com.reviq.multitenancy.config.MultiTenantLiquibaseConfig;
import com.reviq.multitenancy.config.TenantClientAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({TenantClientAutoConfiguration.class, MultiTenantLiquibaseConfig.class})
public @interface EnableMultitenancyLib {
}
