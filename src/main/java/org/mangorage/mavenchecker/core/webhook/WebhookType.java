package org.mangorage.mavenchecker.core.webhook;

/**
 * All the types of webhooks we have.
 *
 * Made as an Enum so you can see all available
 * types from the configuration page.
 */
public enum WebhookType {
    DISCORD,
    NORMAL,
    REGEN,
    PROMOTE_LATEST
}
