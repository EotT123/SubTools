package org.lodder.subtools.multisubdownloader.subtitleproviders;

import java.util.HashSet;
import java.util.Set;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.model.Subtitle;

@NullMarked
public class SubtitleProviderStore {
    protected final Set<SubtitleProvider<? extends Subtitle>> subtitleProviders = new HashSet<>();

    @val Set<SubtitleProvider<? extends Subtitle>> allProviders = Set.copyOf(this.subtitleProviders);

    public void addProvider(SubtitleProvider<? extends Subtitle> provider) {
        this.subtitleProviders.add(provider);
    }

    public void deleteProvider(SubtitleProvider<? extends Subtitle> subtitleProvider) {
        this.subtitleProviders.remove(subtitleProvider);
    }
}
