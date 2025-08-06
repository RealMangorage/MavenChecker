package org.mangorage.mavenchecker.core.data.forge;

import org.mangorage.mavenchecker.core.HasJson;

/**
 * Everything needed to regen an Artifact in the Forge Files site
 */
public record ForgeRegenAlert(
        String group,
        String artifact
) implements HasJson {
    public static ForgeRegenAlert of(String group, String artifact) {
        return new ForgeRegenAlert(group, artifact);
    }
}
