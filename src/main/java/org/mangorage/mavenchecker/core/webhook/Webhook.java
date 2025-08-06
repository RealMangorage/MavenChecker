package org.mangorage.mavenchecker.core.webhook;

import org.mangorage.mavenchecker.core.ActionType;

/**
 * All the info we need for webhooks.
 */
public record Webhook(
        String id,
        ActionType actionType,
        WebhookType webhookType,

        String username,
        String password,
        String url,
        boolean enabled
) {}
