package org.mangorage.mavenchecker.data;

public record GeneralWebhook(
                             String username,
                             String password,
                             String url,
                             boolean enabled
) implements Webhook {}
