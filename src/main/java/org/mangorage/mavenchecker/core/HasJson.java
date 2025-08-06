package org.mangorage.mavenchecker.core;

/**
 * Used to get Json Data from a global {@link com.google.gson.Gson} instance
 */
public interface HasJson {
    default String toJson() {
        return Constants.GSON.toJson(this, this.getClass());
    }
}
