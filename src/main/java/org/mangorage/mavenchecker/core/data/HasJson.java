package org.mangorage.mavenchecker.core.data;

import org.mangorage.mavenchecker.core.Constants;

public interface HasJson {
    default String toJson() {
        return Constants.GSON.toJson(this, this.getClass());
    }
}
