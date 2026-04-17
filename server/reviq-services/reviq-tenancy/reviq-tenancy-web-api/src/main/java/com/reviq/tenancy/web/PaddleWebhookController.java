package com.reviq.tenancy.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviq.tenancy.domain.entity.ProcessedWebhookEvent;
import com.reviq.tenancy.domain.repository.ProcessedWebhookEventRepository;
import com.reviq.tenancy.infrastructure.payment.paddle.PaddleService;
import com.reviq.tenancy.infrastructure.payment.paddle.PaddleSubscriptionSyncService;
import com.reviq.tenancy.infrastructure.payment.paddle.dto.PaddleWebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class PaddleWebhookController {

    private final PaddleService paddleService;
    private final PaddleSubscriptionSyncService syncService;
    private final ProcessedWebhookEventRepository webhookEventRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/paddle")
    public ResponseEntity<String> handlePaddleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Paddle-Signature", required = false) String signature) {

        try {
            if (signature != null && !paddleService.verifyWebhookSignature(payload, signature)) {
                log.warn("Invalid Paddle webhook signature");
                return ResponseEntity.ok("ignored");
            }

            PaddleWebhookEvent event = objectMapper.readValue(payload, PaddleWebhookEvent.class);

            if (webhookEventRepository.existsByEventId(event.eventId())) {
                log.debug("Webhook event already processed: {}", event.eventId());
                return ResponseEntity.ok("already_processed");
            }

            log.info("Processing Paddle webhook: {} ({})", event.eventType(), event.eventId());
            processEvent(event);
            markProcessed(event);

        } catch (Exception e) {
            log.error("Error processing Paddle webhook: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok("ok");
    }

    private void processEvent(PaddleWebhookEvent event) {
        Map<String, Object> data = event.data();

        switch (event.eventType()) {
            case PaddleWebhookEvent.SUBSCRIPTION_CREATED,
                 PaddleWebhookEvent.SUBSCRIPTION_UPDATED,
                 PaddleWebhookEvent.SUBSCRIPTION_TRIALING -> syncService.syncSubscriptionFromWebhook(data);

            case PaddleWebhookEvent.SUBSCRIPTION_ACTIVATED,
                 PaddleWebhookEvent.SUBSCRIPTION_RESUMED -> syncService.activateSubscription(data);

            case PaddleWebhookEvent.SUBSCRIPTION_CANCELED,
                 PaddleWebhookEvent.SUBSCRIPTION_PAUSED -> syncService.cancelSubscription(data);

            case PaddleWebhookEvent.SUBSCRIPTION_PAST_DUE -> syncService.markPastDue(data);

            case PaddleWebhookEvent.TRANSACTION_COMPLETED,
                 PaddleWebhookEvent.TRANSACTION_PAID -> syncService.handlePaymentCompleted(data);

            case PaddleWebhookEvent.TRANSACTION_PAYMENT_FAILED ->
                    log.warn("Payment failed for transaction: {}", data.get("id"));

            default -> log.debug("Unhandled webhook event type: {}", event.eventType());
        }
    }

    private void markProcessed(PaddleWebhookEvent event) {
        webhookEventRepository.save(ProcessedWebhookEvent.builder()
                .eventId(event.eventId())
                .eventType(event.eventType())
                .source("paddle")
                .processedAt(LocalDateTime.now())
                .build());
    }
}
