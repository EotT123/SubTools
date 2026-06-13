package org.lodder.subtools.multisubdownloader.serviceprovider;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleprovider.local.Local;

@NullMarked
public final class LocalServiceProvider implements ServiceProvider {

    @Override
    public Supplier<Local> createProviderSupplier(UserInteractionHandler userInteractionHandler) {
        return () -> new Local(userInteractionHandler);
    }
}
