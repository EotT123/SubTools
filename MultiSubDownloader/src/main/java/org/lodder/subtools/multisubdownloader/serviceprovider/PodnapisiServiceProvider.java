package org.lodder.subtools.multisubdownloader.serviceprovider;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleprovider.podnapisi.PodnapisiAdapter;

@NullMarked
public final class PodnapisiServiceProvider implements ServiceProvider {

    @Override
    public Supplier<PodnapisiAdapter> createProviderSupplier(UserInteractionHandler userInteractionHandler) {
        return () -> new PodnapisiAdapter(userInteractionHandler);
    }
}
