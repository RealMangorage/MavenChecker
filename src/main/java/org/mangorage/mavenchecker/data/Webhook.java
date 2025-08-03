package org.mangorage.mavenchecker.data;

public record Webhook(
        String id,
        String triggerAction,
        WebhookType webhookType,

        String username,
        String password,
        String url,
        boolean enabled
) {
    @Override
    public String toString() {
        return "Test";
    }
}
