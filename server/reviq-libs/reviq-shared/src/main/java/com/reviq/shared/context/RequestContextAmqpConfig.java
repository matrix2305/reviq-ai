package com.reviq.shared.context;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configures RequestContext propagation for RabbitMQ:
 * - Outbound: injects context headers on every publish
 * - Inbound: restores context before listener, clears after
 * - Extensible: modules can contribute additional advice via RabbitListenerAdviceContributor
 */
@Slf4j
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
public class RequestContextAmqpConfig {

    @Autowired(required = false)
    private List<RabbitTemplate> rabbitTemplates;

    @Autowired(required = false)
    private List<RabbitListenerAdviceContributor> adviceContributors;

    @PostConstruct
    public void configureOutboundContextPropagation() {
        if (rabbitTemplates != null) {
            rabbitTemplates.forEach(template ->
                    template.addBeforePublishPostProcessors(RequestContextAmqpSupport.outbound()));
            log.debug("Configured RequestContext outbound propagation on {} RabbitTemplate(s)", rabbitTemplates.size());
        }
    }

    @Bean
    @Primary
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAfterReceivePostProcessors(message -> {
            RequestContextAmqpSupport.restoreContext(message);
            return message;
        });
        factory.setAdviceChain(buildAdviceChain());
        return factory;
    }

    private MethodInterceptor[] buildAdviceChain() {
        List<MethodInterceptor> chain = new ArrayList<>();

        // Module-contributed advice (e.g., tenant DataSource resolution)
        if (adviceContributors != null) {
            adviceContributors.forEach(c -> chain.add(c.getAdvice()));
        }

        // RequestContext cleanup — always last
        chain.add(invocation -> {
            try {
                return invocation.proceed();
            } finally {
                RequestContext.clear();
            }
        });

        return chain.toArray(new MethodInterceptor[0]);
    }
}
