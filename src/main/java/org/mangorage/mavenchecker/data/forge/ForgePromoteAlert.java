package org.mangorage.mavenchecker.data.forge;

import org.mangorage.mavenchecker.data.HasJson;

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
