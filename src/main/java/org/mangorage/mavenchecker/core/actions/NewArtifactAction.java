package org.mangorage.mavenchecker.core.actions;

import com.reposilite.maven.api.DeployEvent;
import com.reposilite.plugin.Extensions;
import com.reposilite.plugin.api.ReposiliteDisposeEvent;
import com.reposilite.plugin.api.ReposilitePlugin;
import com.reposilite.plugin.api.ReposilitePostInitializeEvent;
import org.mangorage.mavenchecker.core.ActionType;
import org.mangorage.mavenchecker.core.data.artifact.ArtifactInfo;
import org.mangorage.mavenchecker.core.data.artifact.ArtifactSnapshotData;
import org.mangorage.mavenchecker.core.data.forge.ForgePromoteAlert;
import org.mangorage.mavenchecker.core.data.forge.ForgeRegenAlert;
import org.mangorage.mavenchecker.core.settings.MavenCheckerSettings;
import org.mangorage.mavenchecker.core.settings.SettingsHolder;
import org.mangorage.mavenchecker.core.webhook.Webhook;
import org.mangorage.mavenchecker.core.webhook.WebhookHelper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class NewArtifactAction {
    public static final NewArtifactAction INSTANCE = new NewArtifactAction();

    private final Map<ArtifactInfo, ArtifactSnapshotData> infoCache = new ConcurrentHashMap<>();
    private SettingsHolder<MavenCheckerSettings> settingsHolder;
    private Extensions extensions;

    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> task;

    NewArtifactAction() {}

    public void init(ReposilitePlugin plugin) {
        this.extensions = plugin.extensions();

        extensions().getLogger().debug("Init NewArtifact Action");


        settingsHolder = SettingsHolder.of(MavenCheckerSettings.class, extensions());
        final var settings = settingsHolder.get();

        extensions().registerEvent(
                ReposilitePostInitializeEvent.class,
                event -> {
                    executorService = event.getReposilite().getScheduler();
                    scheduleTask(settings.getCheckRate());
                }
        );

        extensions().registerEvent(DeployEvent.class, event -> {
            if (!event.getGav().contains("/maven-metadata.xml")) {
                final ArtifactInfo info = extractGAV(extensions(), event.getGav().toString());
                if (info != null) {
                    final var timeStamp = System.currentTimeMillis();
                    final var data = infoCache.computeIfAbsent(info, a -> new ArtifactSnapshotData(event.getBy(), timeStamp));
                    data.getLocationList().add(event.getGav());
                    data.setLastUpdated(timeStamp);
                }
            }
        });

        settingsHolder.getReference().subscribe(newSettings -> {
            task.cancel(false);
            if (task.isCancelled() || task.isDone()) {
                extensions().getLogger().debug("Cancelled our MavenCheck task successfully");
            }
            scheduleTask(newSettings.getCheckRate());
        });
    }

    Extensions extensions() {
        return extensions;
    }

    void newArtifacts(final ArtifactInfo info, final ArtifactSnapshotData data) {
        extensions().getLogger().info("Got new Artifact (Took %s ms) -> ".formatted(data.getLastUpdated() - data.getCreated()) + info.asString());
        data.getLocationList().forEach(location -> {
            extensions().getLogger().info(location.toString());
        });

        final var settings = settingsHolder.get();

        settings.getWebhooks()
                .stream()
                .filter(Webhook::enabled)
                .filter(webhook -> webhook.actionType() == ActionType.NEW_ARTIFACTS)
                .forEach(webhook -> {
                    switch (webhook.webhookType()) {
                        case NORMAL -> WebhookHelper.sendWebhook(webhook, data.asFinalData(info));
                        case DISCORD -> WebhookHelper.sendDiscordWebhook(info, data, webhook);
                        case REGEN -> WebhookHelper.sendWebhook(webhook, ForgeRegenAlert.of(info.group(), info.artifact()));
                        case PROMOTE_LATEST -> WebhookHelper.sendWebhook(webhook, ForgePromoteAlert.latest(info.group(), info.artifact(), info.version()));
                    }
                });
    }


    void task() {
        infoCache.forEach((info, data) -> {
            // Wait atleast 5 seconds to ensure we got everything!
            if (System.currentTimeMillis() - data.getLastUpdated() > settingsHolder.get().getLastUpdatedAge()) {
                newArtifacts(info, data);
                infoCache.remove(info);
            }
        });
    }

    void scheduleTask( long checkRate) {
        extensions().getLogger().debug("Scheduled our task to be ran every %sms".formatted(checkRate));
        task = executorService.scheduleAtFixedRate(this::task, 0, checkRate, TimeUnit.MILLISECONDS);
    }

    public static ArtifactInfo extractGAV(Extensions extensions, String path) {
        // Split that string like your grades split your parents
        String[] parts = path.split("/");

        if (parts.length < 4) {
            extensions.getLogger().error("Failed to get group/artifact/version info");
            return null;
        }

        String version = parts[parts.length - 2];
        String artifact = parts[parts.length - 3];
        StringBuilder groupBuilder = new StringBuilder();

        for (int i = 0; i < parts.length - 3; i++) {
            if (i > 0) groupBuilder.append('.');
            groupBuilder.append(parts[i]);
        }

        return new ArtifactInfo(
                groupBuilder.toString(), // group
                artifact,                // artifact
                version                  // version
        );
    }
}
