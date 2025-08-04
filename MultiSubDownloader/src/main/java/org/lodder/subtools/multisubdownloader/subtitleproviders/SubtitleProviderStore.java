package org.lodder.subtools.multisubdownloader.subtitleproviders;

import java.util.HashSet;
import java.util.Set;

import org.lodder.subtools.sublibrary.model.Subtitle;

public class SubtitleProviderStore {
    protected final Set<SubtitleProvider<? extends Subtitle>> subtitleProviders = new HashSet<>();

    public Set<SubtitleProvider<? extends Subtitle>> getAllProviders() {
        return new HashSet<>(this.subtitleProviders);
    }

    public void addProvider(SubtitleProvider<? extends Subtitle> provider) {
        this.subtitleProviders.add(provider);
    }

    public void deleteProvider(SubtitleProvider<? extends Subtitle> subtitleProvider) {
        this.subtitleProviders.remove(subtitleProvider);
    }
}
