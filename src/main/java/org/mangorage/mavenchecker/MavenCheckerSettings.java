package org.mangorage.mavenchecker;

import com.reposilite.configuration.shared.api.Doc;
import com.reposilite.configuration.shared.api.SharedSettings;
import io.javalin.openapi.JsonSchema;
import org.mangorage.mavenchecker.data.DiscordWebhook;
import org.mangorage.mavenchecker.data.forge.ForgeWebhook;
import org.mangorage.mavenchecker.data.GeneralWebhook;

@JsonSchema(requireNonNulls = false)
@Doc(title = "MavenChecker Settings", description = "MavenChecker settings")
public final class MavenCheckerSettings implements SharedSettings {
    public long lastUpdatedAge = 250;
    public long checkRate = 1000;

    public DiscordWebhook discordWebhook = new DiscordWebhook(null, false);
    public GeneralWebhook webhook = new GeneralWebhook(null, null, null, false);
    public ForgeWebhook forgeWebhook = new ForgeWebhook(null, null, null, false);

    @Doc(
            title = "LastUpdatedAge",
            description = """ 
                    How long do you want to wait for all the files 
                    to be deployed for a specific artifact, 
                    
                    e.g org.mangorage:example:1.0.0
                    """
    )
    public long getLastUpdatedAge() {
        return lastUpdatedAge;
    }

    @Doc(
            title = "GetCheckRate",
            description = "How often do you want to check for new artifacts being published"
    )
    public long getCheckRate() {
        return checkRate;
    }

    @Doc(
            title = "Discord Webhook",
            description = "Where do you want to send the discord notifications..."
    )
    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    @Doc(
            title = "General Webhook",
            description = "Where do you want to trigger something?"
    )
    public GeneralWebhook getWebhook() {
        return webhook;
    }

    @Doc(
            title = "Forge Webhook",
            description = "Used to regen files page on Forge"
    )
    public ForgeWebhook getForgeWebhook() {
        return forgeWebhook;
    }
}
