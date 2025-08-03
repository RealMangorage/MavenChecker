package org.mangorage.mavenchecker.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Constants {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String VERSION = "1.0.0";
}
