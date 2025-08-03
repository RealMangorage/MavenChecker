package org.mangorage.mavenchecker.data;

import org.mangorage.mavenchecker.helper.Constants;

public interface HasJson {
    default String toJson() {
        return Constants.GSON.toJson(this, this.getClass());
    }
}
