package org.mangorage.mavenchecker.core.data;

import com.reposilite.storage.api.Location;
import org.mangorage.mavenchecker.MavenCheckerPlugin;

import java.util.List;

public record ArtifactData(
        String group,
        String artifactId,
        String version,
        List<String> files
) implements HasJson {
    public static ArtifactData of(MavenCheckerPlugin.Info info, MavenCheckerPlugin.Data data) {
        return new ArtifactData(
                info.group(),
                info.artifact(),
                info.version(),
                data.locations()
                        .stream()
                        .map(Location::toString)
                        .toList()
        );
    }
}
