package org.lodder.subtools.multisubdownloader.serviceproviders;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.SubsceneAdapter;

@NullMarked
public class SubsceneServiceProvider implements ServiceProvider {

    @Override
    public Supplier<SubsceneAdapter> createProviderSupplier(UserInteractionHandler userInteractionHandler) {
        return () -> new SubsceneAdapter(userInteractionHandler);
    }
}
