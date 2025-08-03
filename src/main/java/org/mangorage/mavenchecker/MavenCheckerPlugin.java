package org.mangorage.mavenchecker;

import com.reposilite.maven.api.DeployEvent;
import com.reposilite.plugin.api.Facade;
import com.reposilite.plugin.api.Plugin;
import com.reposilite.plugin.api.ReposilitePlugin;
import com.reposilite.storage.api.Location;
import org.jetbrains.annotations.Nullable;
import org.mangorage.mavenchecker.core.data.ActionType;
import org.mangorage.mavenchecker.core.data.ArtifactData;
import org.mangorage.mavenchecker.core.data.Webhook;
import org.mangorage.mavenchecker.core.data.forge.ForgePromoteAlert;
import org.mangorage.mavenchecker.core.data.forge.ForgeRegenAlert;
import org.mangorage.mavenchecker.core.Constants;
import org.mangorage.mavenchecker.core.SettingsHolder;
import org.mangorage.mavenchecker.core.WebhookHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Plugin(name = "mavenchecker", version = Constants.VERSION, settings = MavenCheckerSettings.class)
public final class MavenCheckerPlugin extends ReposilitePlugin {

    public record Info(String group, String artifact, String version) {
        public String asString() {
            return "%s:%s:%s".formatted(group, artifact, version);
        }    }

    /**
     * @param created → Defines when we first created this object...
     */
    public record Data(long created, AtomicLong lastUpdated, List<Location> locations) {}


    public final Map<Info, Data> infoCache = new ConcurrentHashMap<>();
    private SettingsHolder<MavenCheckerSettings> settingsHolder;

    public void newArtifacts(Info info, Data data) {
        extensions().getLogger().info("Got new Artifact (Took %s ms) -> ".formatted(data.lastUpdated().get() - data.created()) + info.asString());
        data.locations().forEach(location -> {
            extensions().getLogger().info(location.toString());
        });

        final var settings = settingsHolder.get();

        settings.getWebhooks()
                .stream()
                .filter(Webhook::enabled)
                .filter(webhook -> webhook.actionType() == ActionType.NEW_ARTIFACTS)
                .forEach(webhook -> {
                    switch (webhook.webhookType()) {
                        case NORMAL -> WebhookHelper.sendWebhook(webhook, ArtifactData.of(info, data));
                        case DISCORD -> WebhookHelper.sendDiscordWebhook(info, data, webhook);
                        case REGEN -> WebhookHelper.sendWebhook(webhook, ForgeRegenAlert.of(info.group(), info.artifact()));
                        case PROMOTE_LATEST -> WebhookHelper.sendWebhook(webhook, ForgePromoteAlert.latest(info.group(), info.artifact(), info.version()));
                    }
                });
    }

    @Override
    public @Nullable Facade initialize() {
        extensions().getLogger().debug("Init MavenChecker Plugin");

        settingsHolder = SettingsHolder.of(MavenCheckerSettings.class, extensions());

        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                try {
                    infoCache.forEach((info, data) -> {
                        // Wait atleast 5 seconds to ensure we got everything!
                        if (System.currentTimeMillis() - data.lastUpdated().get() > settingsHolder.get().getLastUpdatedAge()) {
                            newArtifacts(info, data);
                            infoCache.remove(info);
                        }
                    });
                    Thread.sleep(settingsHolder.get().getCheckRate()); // Check every 10 seconds...
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        extensions().registerEvent(DeployEvent.class, event -> {
            extensions().getLogger().debug("START");
            extensions().getLogger().debug(event.getGav());
            extensions().getLogger().debug("END");

            if (!event.getGav().contains("/maven-metadata.xml")) {
                final Info info = extractGAV(event.getGav().toString());
                if (info != null) {
                    final var timeStamp = System.currentTimeMillis();
                    final var data = infoCache.computeIfAbsent(info, a -> new Data(timeStamp, new AtomicLong(timeStamp), new ArrayList<>()));
                    data.locations().add(event.getGav());
                    data.lastUpdated().set(timeStamp);
                }
            }
        });

        return null;
    }

    public Info extractGAV(String path) {
        // Split that string like your grades split your parents
        String[] parts = path.split("/");

        if (parts.length < 4) {
            extensions().getLogger().error("Failed to get group/artifact/version info");
            return null;
        }

        String version = parts[parts.length - 2];
        String artifact = parts[parts.length - 3];
        StringBuilder groupBuilder = new StringBuilder();

        for (int i = 0; i < parts.length - 3; i++) {
            if (i > 0) groupBuilder.append('.');
            groupBuilder.append(parts[i]);
        }

        return new Info(
                groupBuilder.toString(), // group
                artifact,                // artifact
                version                  // version
        );
    }
}
