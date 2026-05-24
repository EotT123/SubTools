package org.lodder.subtools.multisubdownloader.serviceproviders;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.PodnapisiAdapter;

@NullMarked
public class PodnapisiServiceProvider implements ServiceProvider {

    @Override
    public Supplier<PodnapisiAdapter> createProviderSupplier(UserInteractionHandler userInteractionHandler) {
        return () -> new PodnapisiAdapter(userInteractionHandler);
    }
}
