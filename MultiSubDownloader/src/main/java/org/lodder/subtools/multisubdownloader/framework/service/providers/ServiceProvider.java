package org.lodder.subtools.multisubdownloader.framework.service.providers;

import java.util.function.Supplier;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.Container;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;

@NullMarked
public interface ServiceProvider {

    @val int priority;

    default void register(Container app, UserInteractionHandler userInteractionHandler) {
        SubtitleProviderStore.registerProvider(createProviderSupplier(app, userInteractionHandler));
    }

    Supplier<? extends SubtitleProvider> createProviderSupplier(Container app,
        UserInteractionHandler userInteractionHandler);
}


