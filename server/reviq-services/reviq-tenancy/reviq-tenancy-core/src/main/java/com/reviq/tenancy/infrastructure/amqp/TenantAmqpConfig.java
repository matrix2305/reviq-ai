package com.reviq.tenancy.infrastructure.amqp;

import com.reviq.tenancy.api.event.TenantEventConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TenantAmqpConfig {

    @Bean
    public TopicExchange tenantExchange() {
        return new TopicExchange(TenantEventConstants.EXCHANGE);
    }

    @Bean
    public Queue provisioningManagementQueue() {
        return new Queue(TenantEventConstants.QUEUE_PROVISIONING_MANAGEMENT, true);
    }

    @Bean
    public Queue provisioningAiQueue() {
        return new Queue(TenantEventConstants.QUEUE_PROVISIONING_AI, true);
    }

    @Bean
    public Queue provisioningResultsQueue() {
        return new Queue(TenantEventConstants.QUEUE_PROVISIONING_RESULTS, true);
    }

    @Bean
    public Queue provisioningCompletedQueue() {
        return new Queue(TenantEventConstants.QUEUE_PROVISIONING_COMPLETED, true);
    }

    @Bean
    public Binding managementBinding(Queue provisioningManagementQueue, TopicExchange tenantExchange) {
        return BindingBuilder.bind(provisioningManagementQueue)
                .to(tenantExchange)
                .with(TenantEventConstants.ROUTING_PROVISIONING_REQUESTED);
    }

    @Bean
    public Binding aiBinding(Queue provisioningAiQueue, TopicExchange tenantExchange) {
        return BindingBuilder.bind(provisioningAiQueue)
                .to(tenantExchange)
                .with(TenantEventConstants.ROUTING_PROVISIONING_REQUESTED);
    }

    @Bean
    public Binding resultsBinding(Queue provisioningResultsQueue, TopicExchange tenantExchange) {
        return BindingBuilder.bind(provisioningResultsQueue)
                .to(tenantExchange)
                .with("tenant.provisioning.*.done");
    }

    @Bean
    public Binding completedBinding(Queue provisioningCompletedQueue, TopicExchange tenantExchange) {
        return BindingBuilder.bind(provisioningCompletedQueue)
                .to(tenantExchange)
                .with(TenantEventConstants.ROUTING_PROVISIONING_COMPLETED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
