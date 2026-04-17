package com.reviq.tenancy.infrastructure.payment.paddle;

import com.reviq.tenancy.infrastructure.config.PaddleConfig;
import com.reviq.tenancy.infrastructure.payment.paddle.dto.PaddleCustomer;
import com.reviq.tenancy.infrastructure.payment.paddle.dto.PaddleResponse;
import com.reviq.tenancy.infrastructure.payment.paddle.dto.PaddleSubscription;
import com.reviq.tenancy.infrastructure.payment.paddle.dto.PaddleTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaddleService {

    private final WebClient paddleWebClient;
    private final PaddleConfig config;

    public PaddleSubscription getSubscription(String subscriptionId) {
        var response = paddleWebClient.get()
                .uri("/subscriptions/{id}", subscriptionId)
                .retrieve()
                .bodyToMono(PaddleResponse.class)
                .block();
        return response != null ? (PaddleSubscription) response.getData() : null;
    }

    public void cancelSubscription(String subscriptionId, boolean immediately) {
        Map<String, Object> body = immediately
                ? Map.of("effective_from", "immediately")
                : Map.of("effective_from", "next_billing_period");

        paddleWebClient.post()
                .uri("/subscriptions/{id}/cancel", subscriptionId)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(PaddleResponse.class)
                .block();
    }

    public void updateSubscription(String subscriptionId, String priceId) {
        Map<String, Object> body = Map.of(
                "items", List.of(Map.of("price_id", priceId, "quantity", 1)),
                "proration_billing_mode", "prorated_immediately"
        );

        paddleWebClient.patch()
                .uri("/subscriptions/{id}", subscriptionId)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(PaddleResponse.class)
                .block();
    }

    public PaddleCustomer createCustomer(String email, String name, Map<String, Object> customData) {
        Map<String, Object> body = Map.of(
                "email", email,
                "name", name,
                "custom_data", customData
        );

        var response = paddleWebClient.post()
                .uri("/customers")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(PaddleResponse.class)
                .block();
        return response != null ? (PaddleCustomer) response.getData() : null;
    }

    public PaddleTransaction createTransaction(String customerId, String priceId,
                                                Map<String, Object> customData) {
        Map<String, Object> body = Map.of(
                "customer_id", customerId,
                "items", List.of(Map.of("price_id", priceId, "quantity", 1)),
                "custom_data", customData
        );

        var response = paddleWebClient.post()
                .uri("/transactions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(PaddleResponse.class)
                .block();
        return response != null ? (PaddleTransaction) response.getData() : null;
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            String[] parts = signature.split(";");
            String ts = parts[0].replace("ts=", "");
            String h1 = parts[1].replace("h1=", "");

            String signedPayload = ts + ":" + payload;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    config.getWebhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);

            return MessageDigest.isEqual(
                    computed.getBytes(StandardCharsets.UTF_8),
                    h1.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}
