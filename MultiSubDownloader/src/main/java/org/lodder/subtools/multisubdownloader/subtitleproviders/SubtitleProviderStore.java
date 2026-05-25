package org.lodder.subtools.multisubdownloader.subtitleproviders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.lazy.LazyObject;
import org.lodder.subtools.sublibrary.util.lazy.LazyObjectThrow;

@NullMarked
public class SubtitleProviderStore {

    private static final Map<String, LazyObject<? extends SubtitleProvider>> PROVIDERS = new HashMap<>();

    public static List<? extends SubtitleProvider> getProviders() {
        return PROVIDERS.values().map(LazyObjectThrow::get).toList();
    }

    public static void registerProvider(Supplier<? extends SubtitleProvider> provider) {
        LazyObject<? extends SubtitleProvider> subtitleProviderLazyObject = new LazyObject<>(provider);
        PROVIDERS.put(subtitleProviderLazyObject.get().provider, subtitleProviderLazyObject);
    }

    public static void resetProviders() {
        PROVIDERS.values().forEach(LazyObject::reset);
    }
}
