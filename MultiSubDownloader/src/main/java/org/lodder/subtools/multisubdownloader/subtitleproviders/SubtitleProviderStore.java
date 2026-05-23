package org.lodder.subtools.multisubdownloader.subtitleproviders;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.lazy.LazyObject;

@NullMarked
public class SubtitleProviderStore {

    private static final Set<SubtitleProviderElement> PROVIDERS = new HashSet<>();

    public static List<? extends SubtitleProvider> getAllProviders() {
        return PROVIDERS.stream().map(sp -> sp.subtitleProvider.get()).toList();
    }

    public static void registerProvider(Supplier<? extends SubtitleProvider> provider) {
        PROVIDERS.add(new SubtitleProviderElement(provider));
    }

    public static void resetProviders() {
        PROVIDERS.forEach(SubtitleProviderElement::reset);
    }

    private record SubtitleProviderElement(LazyObject<? extends SubtitleProvider> subtitleProvider) {

        public SubtitleProviderElement(Supplier<? extends SubtitleProvider> supplier) {
            LazyObject<? extends SubtitleProvider> subtitleProvider = new LazyObject<>(supplier);
            this(subtitleProvider);
        }

        public void reset() {
            subtitleProvider.reset();
        }

        @Override
        public boolean equals(Object o) {
            return this == o || (o instanceof SubtitleProviderElement that
                && Objects.equals(subtitleProvider.get().provider, that.subtitleProvider.get().provider));
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(subtitleProvider.get().provider);
        }
    }
}
