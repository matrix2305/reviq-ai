package com.reviq.tenancy.api.event;

public final class TenantEventConstants {

    public static final String EXCHANGE = "reviq.tenancy";

    public static final String ROUTING_PROVISIONING_REQUESTED = "tenant.provisioning.requested";
    public static final String ROUTING_PROVISIONING_MANAGEMENT_DONE = "tenant.provisioning.management.done";
    public static final String ROUTING_PROVISIONING_AI_DONE = "tenant.provisioning.ai.done";
    public static final String ROUTING_PROVISIONING_COMPLETED = "tenant.provisioning.completed";

    public static final String QUEUE_PROVISIONING_MANAGEMENT = "tenancy.provisioning.management";
    public static final String QUEUE_PROVISIONING_AI = "tenancy.provisioning.ai";
    public static final String QUEUE_PROVISIONING_RESULTS = "tenancy.provisioning.results";
    public static final String QUEUE_PROVISIONING_COMPLETED = "tenancy.provisioning.completed";

    private TenantEventConstants() {
    }
}
