package com.reviq.shared.context;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;

import java.util.Set;
import java.util.UUID;

/**
 * Propagates RequestContext through RabbitMQ message headers.
 *
 * Publisher side: use outbound() as MessagePostProcessor to inject context into headers.
 * Consumer side: call restoreContext(message) at start, clearContext() in finally.
 */
public final class RequestContextAmqpSupport {

    private static final String HEADER_USER_ID = "x-context-user-id";
    private static final String HEADER_EMAIL = "x-context-email";
    private static final String HEADER_ROLE = "x-context-role";
    private static final String HEADER_TENANT_CODE = "x-context-tenant-code";
    private static final String HEADER_TENANT_ID = "x-context-tenant-id";
    private static final String HEADER_PERMISSIONS = "x-context-permissions";

    private RequestContextAmqpSupport() {}

    public static MessagePostProcessor outbound() {
        return message -> {
            RequestContext.RequestContextData data = RequestContext.get();
            if (data == null) return message;

            MessageProperties props = message.getMessageProperties();
            if (data.getUserId() != null) props.setHeader(HEADER_USER_ID, data.getUserId().toString());
            if (data.getEmail() != null) props.setHeader(HEADER_EMAIL, data.getEmail());
            if (data.getRole() != null) props.setHeader(HEADER_ROLE, data.getRole());
            if (data.getTenantCode() != null) props.setHeader(HEADER_TENANT_CODE, data.getTenantCode());
            if (data.getTenantId() != null) props.setHeader(HEADER_TENANT_ID, data.getTenantId().toString());
            if (data.getPermissions() != null && !data.getPermissions().isEmpty()) {
                props.setHeader(HEADER_PERMISSIONS, String.join(",", data.getPermissions()));
            }

            return message;
        };
    }

    public static void restoreContext(Message message) {
        MessageProperties props = message.getMessageProperties();

        String userId = getHeader(props, HEADER_USER_ID);
        String email = getHeader(props, HEADER_EMAIL);
        String role = getHeader(props, HEADER_ROLE);
        String tenantCode = getHeader(props, HEADER_TENANT_CODE);
        String tenantId = getHeader(props, HEADER_TENANT_ID);
        String permissions = getHeader(props, HEADER_PERMISSIONS);

        RequestContext.set(RequestContext.RequestContextData.builder()
                .userId(userId != null ? UUID.fromString(userId) : null)
                .email(email)
                .role(role)
                .tenantCode(tenantCode)
                .tenantId(tenantId != null ? UUID.fromString(tenantId) : null)
                .permissions(permissions != null ? Set.of(permissions.split(",")) : Set.of())
                .build());
    }

    public static void clearContext() {
        RequestContext.clear();
    }

    private static String getHeader(MessageProperties props, String key) {
        Object val = props.getHeader(key);
        return val != null ? val.toString() : null;
    }
}
