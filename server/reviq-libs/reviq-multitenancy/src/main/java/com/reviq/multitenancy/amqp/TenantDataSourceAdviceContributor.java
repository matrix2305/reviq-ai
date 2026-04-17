package com.reviq.multitenancy.amqp;

import com.reviq.multitenancy.ConnectionInfo;
import com.reviq.multitenancy.TenantConnectionResolver;
import com.reviq.multitenancy.datasource.TenantDataSourceCache;
import com.reviq.multitenancy.datasource.TenantDataSourceHolder;
import com.reviq.shared.context.RabbitListenerAdviceContributor;
import com.reviq.shared.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * Resolves tenant DataSource for RabbitMQ listeners in multi-tenant mode.
 * Equivalent of TenantInterceptor but for async message processing.
 */
@Slf4j
@RequiredArgsConstructor
public class TenantDataSourceAdviceContributor implements RabbitListenerAdviceContributor {

    private final TenantConnectionResolver connectionResolver;
    private final TenantDataSourceCache dataSourceCache;

    @Override
    public MethodInterceptor getAdvice() {
        return invocation -> {
            String tenantCode = RequestContext.getTenantCode();

            if (tenantCode != null && !tenantCode.isBlank()) {
                log.debug("Resolving tenant DataSource for RabbitMQ listener: {}", tenantCode);
                ConnectionInfo info = connectionResolver.resolve(tenantCode);
                var ds = dataSourceCache.getOrCreate(tenantCode, info);
                TenantDataSourceHolder.set(ds);
            }

            try {
                return invocation.proceed();
            } finally {
                TenantDataSourceHolder.clear();
            }
        };
    }
}
