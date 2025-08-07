package org.mangorage.mavenchecker;

import com.reposilite.plugin.api.Facade;
import com.reposilite.plugin.api.Plugin;
import com.reposilite.plugin.api.ReposilitePlugin;
import org.jetbrains.annotations.Nullable;
import org.mangorage.mavenchecker.core.actions.NewArtifactAction;
import org.mangorage.mavenchecker.core.settings.MavenCheckerSettings;
import org.mangorage.mavenchecker.core.Constants;

@Plugin(name = "mavenchecker", version = Constants.VERSION, settings = MavenCheckerSettings.class)
public final class MavenCheckerPlugin extends ReposilitePlugin {

    @Override
    public @Nullable Facade initialize() {
        extensions().getLogger().debug("Init MavenChecker Plugin");
        NewArtifactAction.INSTANCE.init(this);
        return null;
    }
}
