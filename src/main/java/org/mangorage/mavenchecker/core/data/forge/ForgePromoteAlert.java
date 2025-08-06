package org.mangorage.mavenchecker.core.data.forge;

import org.mangorage.mavenchecker.core.HasJson;

/**
 * Everything needed to promote an Artifact in the Forge Files site
 */
public record ForgePromoteAlert(
        String group,
        String artifact,
        String version,
        String type
) implements HasJson {
    public static ForgePromoteAlert latest(String group, String artifact, String version) {
        return new ForgePromoteAlert(group, artifact, version, "latest");
    }

    public static ForgePromoteAlert recommended(String group, String artifact, String version) {
        return new ForgePromoteAlert(group, artifact, version, "recommended");
    }
}
