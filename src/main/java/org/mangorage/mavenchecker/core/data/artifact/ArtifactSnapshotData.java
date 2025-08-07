package org.mangorage.mavenchecker.core.data.artifact;

import com.reposilite.storage.api.Location;

import java.util.ArrayList;
import java.util.List;

public final class ArtifactSnapshotData {
    private final String user;
    private final List<Location> locationList = new ArrayList<>();
    private final long created;
    private long lastUpdated;

    public ArtifactSnapshotData(final String user, final long created) {
        this.user = user;
        this.created = created;
    }

    public long getCreated() {
        return created;
    }

    public List<Location> getLocationList() {
        return locationList;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public String getUser() {
        return user;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public ArtifactData asFinalData(ArtifactInfo info) {
        return new ArtifactData(
                info.group(),
                info.artifact(),
                info.version(),
                getLocationList()
        );
    }
}
