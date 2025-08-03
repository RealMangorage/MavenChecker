package org.mangorage.mavenchecker;

import com.reposilite.configuration.shared.api.Doc;
import com.reposilite.configuration.shared.api.SharedSettings;
import io.javalin.openapi.JsonSchema;
import org.mangorage.mavenchecker.core.data.Webhook;

import java.util.ArrayList;

@JsonSchema(requireNonNulls = false)
@Doc(title = "MavenChecker Settings", description = "MavenChecker settings")
public final class MavenCheckerSettings implements SharedSettings {
    public long lastUpdatedAge = 250;
    public long checkRate = 1000;

    public ArrayList<Webhook> webhooks;

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
            title = "Webhooks",
            description = "Webhooks you want to use"
    )
    public ArrayList<Webhook> getWebhooks() {
        return webhooks;
    }
}
