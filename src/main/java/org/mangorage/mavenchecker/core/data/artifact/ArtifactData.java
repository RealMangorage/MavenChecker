package org.mangorage.mavenchecker.core.data.artifact;

import com.reposilite.storage.api.Location;
import org.mangorage.mavenchecker.core.HasJson;

import java.util.List;

/**
 * All the info about an artifact and the files it has
 */
public record ArtifactData(
        String group,
        String artifactId,
        String version,
        List<Location> files
) implements HasJson {
    private record ArtifactJsonData(
            String group,
            String artifactId,
            String version,
            List<String> files
    ) implements HasJson {}

    @Override
    public String toJson() {
        return new ArtifactJsonData(
                group(),
                artifactId(),
                version(),
                files()
                        .stream()
                        .map(Location::toString)
                        .toList()
        ).toJson();
    }
}
