package org.mangorage.mavenchecker.helper;

import club.minnced.discord.webhook.WebhookClient;
import org.mangorage.mavenchecker.MavenCheckerPlugin;

public final class WebhookHelper {
    public static void send(MavenCheckerPlugin.Info info, MavenCheckerPlugin.Data data, String url) {
        if (url == null) return;
        try (WebhookClient client = WebhookClient.withUrl(url)) {

            client.send("Got new Artifact (Took %s ms) -> ".formatted(data.lastUpdated().get() - data.created()) + info.asString());
            data.locations().forEach(location -> {
                client.send(location.toString());
            });
        }
    }
}
