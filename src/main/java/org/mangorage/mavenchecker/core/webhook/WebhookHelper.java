package org.mangorage.mavenchecker.core.webhook;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import org.mangorage.mavenchecker.MavenCheckerPlugin;
import org.mangorage.mavenchecker.core.HasJson;
import org.mangorage.mavenchecker.core.data.artifact.ArtifactInfo;
import org.mangorage.mavenchecker.core.data.artifact.ArtifactSnapshotData;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Helper class to handle various webhook implementations
 */
public final class WebhookHelper {

    public static void sendDiscordWebhook(ArtifactInfo info, ArtifactSnapshotData data, Webhook webhook) {
        try (WebhookClient client = WebhookClient.withUrl(webhook.url())) {

            List<String> lines = new ArrayList<>();
            lines.add("Got new Artifact (Took %s ms) -> ".formatted(data.getLastUpdated() - data.getCreated()) + info.asString());
            data.getLocationList().forEach(location -> lines.add(location.toString()));

            List<String> messages = groupLinesIntoMessages(lines, 2000);
            for (String msg : messages) {
                client.send(
                        new WebhookMessageBuilder()
                                .setContent(msg)
                                .setUsername("MavenChecker")
                                .build()
                );
            }
        }

    }

    private static List<String> groupLinesIntoMessages(List<String> lines, int limit) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String line : lines) {
            // +1 for newline character (which will be added)
            if (current.length() + line.length() + 1 > limit) {
                result.add(current.toString());
                current = new StringBuilder();
            }
            if (current.length() > 0) {
                current.append('\n');
            }
            current.append(line);
        }

        if (!current.isEmpty()) {
            result.add(current.toString());
        }

        return result;
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
