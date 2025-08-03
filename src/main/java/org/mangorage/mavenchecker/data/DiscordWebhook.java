package org.mangorage.mavenchecker.data;

public record DiscordWebhook(
        String url,
        boolean enabled
) implements Webhook {
    @Override
    public String username() {
        return null;
    }

    @Override
    public String password() {
        return null;
    }
}
