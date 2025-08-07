package org.mangorage.mavenchecker.core.data.artifact;

public record ArtifactInfo(String group, String artifact, String version) {
    public String asString() {
        return "%s:%s:%s".formatted(group, artifact, version);
    }
}
