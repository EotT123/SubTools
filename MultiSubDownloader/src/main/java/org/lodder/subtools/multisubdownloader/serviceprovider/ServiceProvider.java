package org.lodder.subtools.multisubdownloader.serviceprovider;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;

@NullMarked
public sealed interface ServiceProvider
    permits Addic7edServiceProvider, LocalServiceProvider, OpenSubtitlesServiceProvider, PodnapisiServiceProvider,
    SubdlServiceProvider, SubsceneServiceProvider, TvSubtitlesServiceProvider {

    default void register(UserInteractionHandler userInteractionHandler) {
        SubtitleProviderStore.registerProvider(createProviderSupplier(userInteractionHandler));
    }

    Supplier<? extends SubtitleProvider> createProviderSupplier(UserInteractionHandler userInteractionHandler);
}


