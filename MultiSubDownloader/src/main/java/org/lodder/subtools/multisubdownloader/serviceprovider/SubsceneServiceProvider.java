package org.lodder.subtools.multisubdownloader.serviceprovider;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.SubsceneAdapter;

@NullMarked
public final class SubsceneServiceProvider implements ServiceProvider {

    @Override
    public Supplier<SubsceneAdapter> createProviderSupplier(UserInteractionHandler userInteractionHandler) {
        return () -> new SubsceneAdapter(userInteractionHandler);
    }
}
