package org.lodder.subtools.multisubdownloader.serviceproviders;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.SubdlAdapter;

@NullMarked
public class SubdlServiceProvider implements ServiceProvider {

    @Override
    public Supplier<SubdlAdapter> createProviderSupplier(UserInteractionHandler userInteractionHandler) {
        return () -> new SubdlAdapter(userInteractionHandler);
    }
}
