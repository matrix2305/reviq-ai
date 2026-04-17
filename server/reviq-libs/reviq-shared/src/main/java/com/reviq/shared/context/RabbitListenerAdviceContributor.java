package com.reviq.shared.context;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * SPI for modules to contribute additional advice to the RabbitMQ listener container factory.
 * Implementations are auto-discovered and their advice is added to the advice chain.
 */
public interface RabbitListenerAdviceContributor {

    MethodInterceptor getAdvice();
}
