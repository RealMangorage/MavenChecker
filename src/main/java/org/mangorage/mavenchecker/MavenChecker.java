package org.mangorage.mavenchecker;

import com.reposilite.maven.api.DeployEvent;
import com.reposilite.plugin.api.Event;
import com.reposilite.plugin.api.Facade;
import com.reposilite.plugin.api.Plugin;
import com.reposilite.plugin.api.ReposilitePlugin;
import com.reposilite.storage.api.Location;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Plugin(name = "mavenchecker")
public final class MavenChecker extends ReposilitePlugin {

    public record Info(String group, String artifact, String version) {
        public String asString() {
            return "%s:%s:%s".formatted(group, artifact, version);
        }
    }

    public record Data(long created, List<Location> locations) {}


    public final Map<Info, Data> infoCache = new ConcurrentHashMap<>();

    public void checkCache() {
        infoCache.forEach((info, data) -> {
            // Wait atleast 5 seconds to ensure we got everything!
            if (System.currentTimeMillis() - data.created() > 5000) {
                newArtifacts(info, data);
                infoCache.remove(info);
            }
        });
    }

    public void newArtifacts(Info info, Data data) {
        extensions().getLogger().info("Got new Artifact -> " + info.asString());
        data.locations().forEach(location -> {
            extensions().getLogger().info(location.toString());
        });
    }



    @Override
    public @Nullable Facade initialize() {
        System.out.println("Init Plugin");


        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                try {
                    checkCache();
                    Thread.sleep(1000 * 10); // Check every 10 seconds...
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        extensions().registerEvent(DeployEvent.class, event -> {
            extensions().getLogger().info("START");
            extensions().getLogger().info(event.getGav());

            if (!event.getGav().contains("/maven-metadata.xml")) {
                final Info info = extractGAV(event.getGav().toString());
                if (info != null) {
                    final var created = System.currentTimeMillis();
                    infoCache.computeIfAbsent(info, a -> new Data(created, new ArrayList<>()))
                            .locations()
                            .add(event.getGav());
                }
            }
        });
        return null;
    }

    public Info extractGAV(String path) {
        // Split that string like your grades split your parents
        String[] parts = path.split("/");

        if (parts.length < 4) {
            extensions().getLogger().debug("Failed to get group/artifact/version info");
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
