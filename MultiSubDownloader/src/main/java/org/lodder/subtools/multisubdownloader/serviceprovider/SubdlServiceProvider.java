package org.lodder.subtools.multisubdownloader.serviceprovider;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.SubdlAdapter;

@NullMarked
public final class SubdlServiceProvider implements ServiceProvider {

    @Override
    public Supplier<SubdlAdapter> createProviderSupplier(UserInteractionHandler userInteractionHandler) {
        return () -> new SubdlAdapter(userInteractionHandler);
    }
}
