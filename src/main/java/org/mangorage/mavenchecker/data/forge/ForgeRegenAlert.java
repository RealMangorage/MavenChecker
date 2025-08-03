package org.mangorage.mavenchecker.data.forge;

import org.mangorage.mavenchecker.data.HasJson;

public record ForgeRegenAlert(
        String group,
        String artifact
) implements HasJson {
    public static ForgeRegenAlert of(String group, String artifact) {
        return new ForgeRegenAlert(group, artifact);
    }
}
