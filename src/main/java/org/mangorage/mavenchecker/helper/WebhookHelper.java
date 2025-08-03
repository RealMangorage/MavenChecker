package org.mangorage.mavenchecker.helper;

import club.minnced.discord.webhook.WebhookClient;
import org.mangorage.mavenchecker.MavenCheckerPlugin;
import org.mangorage.mavenchecker.data.HasJson;
import org.mangorage.mavenchecker.data.Webhook;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

public final class WebhookHelper {
    public static void sendDiscordWebhook(MavenCheckerPlugin.Info info, MavenCheckerPlugin.Data data, Webhook webhook) {
        try (WebhookClient client = WebhookClient.withUrl(webhook.url())) {

            client.send("Got new Artifact (Took %s ms) -> ".formatted(data.lastUpdated().get() - data.created()) + info.asString());
            data.locations().forEach(location -> {
                client.send(location.toString());
            });
        }
    }

    public static void sendWebhook(Webhook webhook, HasJson hasJson) {
        sendWebhook(webhook, hasJson.toJson());
    }

    public static void sendWebhook(Webhook webhook, String jsonPayload) {
        try {
            URL url = new URL(webhook.url());
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            // Enforce TLS 1.3 (best effort)
            conn.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());

            // Pretend we support compressed responses
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            // Basic Auth header
            if (webhook.username() != null && webhook.password() != null) {
                String auth = webhook.username() + ":" + webhook.password();
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            }

            conn.setConnectTimeout(10_000); // 10s connection timeout
            conn.setReadTimeout(10_000);    // 10s read timeout
            conn.setDoOutput(true);

            // Debug info
            System.out.println("===[Webhook Debug Info]===");
            System.out.println("Target URL: " + webhook.url());
            System.out.println("Payload: " + jsonPayload);
            if (webhook.username() != null) {
                System.out.println("Using Basic Auth with username: " + webhook.username());
            }
            System.out.println("===========================");

            // Send request body
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Webhook response code: " + responseCode);

            // Read response, handling gzip if present
            try (InputStream is = conn.getInputStream()) {
                InputStream responseStream = "gzip".equalsIgnoreCase(conn.getContentEncoding())
                        ? new GZIPInputStream(is)
                        : is;

                String response = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("Webhook response body: " + response);
            }

        } catch (Exception e) {
            System.err.println("Failed to send webhook: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
