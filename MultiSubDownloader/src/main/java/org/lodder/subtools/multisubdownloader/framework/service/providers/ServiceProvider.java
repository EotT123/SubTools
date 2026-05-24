package org.lodder.subtools.multisubdownloader.framework.service.providers;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;

@NullMarked
public interface ServiceProvider {

    default void register(UserInteractionHandler userInteractionHandler) {
        SubtitleProviderStore.registerProvider(createProviderSupplier(userInteractionHandler));
    }

    Supplier<? extends SubtitleProvider> createProviderSupplier(UserInteractionHandler userInteractionHandler);
}


