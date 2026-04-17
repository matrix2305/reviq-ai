package com.reviq.management.infrastructure.provisioning;

import com.reviq.multitenancy.TenantConnectionResolver;
import com.reviq.multitenancy.provisioning.AbstractTenantProvisioningListener;
import com.reviq.multitenancy.provisioning.SchemaProvisioner;
import com.reviq.tenancy.api.event.TenantEventConstants;
import com.reviq.tenancy.api.event.TenantProvisioningRequestedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TenantProvisioningListener extends AbstractTenantProvisioningListener {

    public TenantProvisioningListener(SchemaProvisioner schemaProvisioner,
                                       TenantConnectionResolver connectionResolver,
                                       RabbitTemplate rabbitTemplate) {
        super(schemaProvisioner, connectionResolver, rabbitTemplate);
    }

    @Override
    protected String getServiceName() {
        return "management";
    }

    @Override
    protected String getRoutingKey() {
        return TenantEventConstants.ROUTING_PROVISIONING_MANAGEMENT_DONE;
    }

    @RabbitListener(queues = TenantEventConstants.QUEUE_PROVISIONING_MANAGEMENT)
    public void onProvisioningRequested(TenantProvisioningRequestedEvent event) {
        handleProvisioningRequest(event);
    }
}
