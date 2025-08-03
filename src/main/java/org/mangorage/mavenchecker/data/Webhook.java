package org.mangorage.mavenchecker.data;

public record Webhook(
        String id,
        ActionType actionType,
        WebhookType webhookType,

        String username,
        String password,
        String url,
        boolean enabled
) {}
