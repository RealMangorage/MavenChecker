package org.mangorage.mavenchecker.core;

import com.reposilite.configuration.shared.SharedConfigurationFacade;
import com.reposilite.configuration.shared.api.SharedSettings;
import com.reposilite.plugin.Extensions;
import kotlin.jvm.JvmClassMappingKt;
import panda.std.reactive.MutableReference;

public final class SettingsHolder<T> {
    public static <T extends SharedSettings> SettingsHolder<T> of(Class<T> tClass, Extensions extensions) {
        final var provider = extensions.facade(SharedConfigurationFacade.class);
        return new SettingsHolder<>(provider.getDomainSettings(JvmClassMappingKt.getKotlinClass(tClass)));
    }

    private final MutableReference<T> reference;

    SettingsHolder(MutableReference<T> reference) {
        this.reference = reference;
    }

    public T get() {
        return reference.get();
    }

    public MutableReference<T> getReference() {
        return reference;
    }
}
