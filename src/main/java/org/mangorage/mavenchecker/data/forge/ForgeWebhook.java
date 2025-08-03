package org.mangorage.mavenchecker.data.forge;

import org.mangorage.mavenchecker.data.Webhook;

public record ForgeWebhook(
        String username,
        String password,
        String url,
        boolean enabled
) implements Webhook {}
