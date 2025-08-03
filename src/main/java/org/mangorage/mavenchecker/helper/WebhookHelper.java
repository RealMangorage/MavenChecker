package org.mangorage.mavenchecker.helper;

import club.minnced.discord.webhook.WebhookClient;
import org.mangorage.mavenchecker.MavenCheckerPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

    public static void sendWebhook(String webhookUrl, String jsonPayload) {
        if (webhookUrl == null) return;
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Webhook response code: " + responseCode);

        } catch (Exception e) {
            System.err.println("Failed to send webhook: " + e.getMessage());
        }
    }

}
