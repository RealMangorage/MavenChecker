package org.mangorage.mavenchecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.reposilite.maven.api.DeployEvent;
import com.reposilite.plugin.api.Facade;
import com.reposilite.plugin.api.Plugin;
import com.reposilite.plugin.api.ReposilitePlugin;
import com.reposilite.storage.api.Location;
import org.jetbrains.annotations.Nullable;
import org.mangorage.mavenchecker.core.Config;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Plugin(name = "mavenchecker")
public final class MavenChecker extends ReposilitePlugin {

    public record Info(String group, String artifact, String version) {
        public String asString() {
            return "%s:%s:%s".formatted(group, artifact, version);
        }
    }

    /**
     * @param created -> Defines when we first created this object...
     */
    public record Data(long created, AtomicLong lastUpdated, List<Location> locations) {}

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path CONFIG_PATH = Path.of("config/mavenchecker.json");

    public final Map<Info, Data> infoCache = new ConcurrentHashMap<>();
    private Config config = new Config(1000, 250);

    public void checkCache() {
        infoCache.forEach((info, data) -> {
            // Wait atleast 5 seconds to ensure we got everything!
            if (System.currentTimeMillis() - data.lastUpdated().get() > config.lastUpdatedAge()) {
                newArtifacts(info, data);
                infoCache.remove(info);
            }
        });
    }

    public void newArtifacts(Info info, Data data) {
        extensions().getLogger().info("Got new Artifact (Took %s ms) -> ".formatted(data.lastUpdated().get() - data.created()) + info.asString());
        data.locations().forEach(location -> {
            extensions().getLogger().info(location.toString());
        });
    }



    @Override
    public @Nullable Facade initialize() {
        extensions().getLogger().debug("Init MavenChecker Plugin");

        try {
            if (Files.exists(CONFIG_PATH)) {
                config = GSON.fromJson(new JsonReader(new FileReader(CONFIG_PATH.toFile())), Config.class);
            } else {
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.write(
                        CONFIG_PATH,
                        GSON.toJson(config).getBytes(),
                        StandardOpenOption.CREATE_NEW
                );
            }
        } catch (IOException exception) {
            extensions().getLogger().error("Failed to handle loading config");
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                try {
                    checkCache();
                    Thread.sleep(config.checkRate()); // Check every 10 seconds...
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
