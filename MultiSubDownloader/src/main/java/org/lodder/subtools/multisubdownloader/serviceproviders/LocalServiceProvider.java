package org.lodder.subtools.multisubdownloader.serviceproviders;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.local.Local;

@NullMarked
public class LocalServiceProvider implements ServiceProvider {

    @Override
    public Supplier<Local> createProviderSupplier(UserInteractionHandler userInteractionHandler) {
        return () -> new Local(userInteractionHandler);
    }
}
