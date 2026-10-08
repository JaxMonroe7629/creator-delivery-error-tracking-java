package com.example.creator.delivery;

import com.example.creator.infrai.InfraiClient;

public final class DeliveryService {
    public record DeliveryRequest(String assetId, String subscriberId, String content) {}
    public record DeliveryResult(String status, String detail) {}
    private final InfraiClient client;
    public DeliveryService(InfraiClient client) { this.client = client; }
    public DeliveryResult deliver(DeliveryRequest request) {
        if (request.content() == null || request.content().isBlank()) return new DeliveryResult("FAILED", "content is empty");
        try {
            if ("processing-error".equals(request.content())) throw new IllegalStateException("content processing failed");
            return new DeliveryResult("DELIVERED", "asset " + request.assetId() + " sent to " + request.subscriberId());
        }
        catch (RuntimeException e) {
            client.capture("creator delivery failed", e.getMessage(), request.assetId() + ":" + request.subscriberId(), e.toString());
            return new DeliveryResult("FAILED", e.getMessage());
        }
    }
}
