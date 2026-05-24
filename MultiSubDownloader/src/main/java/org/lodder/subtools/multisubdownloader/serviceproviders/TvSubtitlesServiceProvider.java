package org.lodder.subtools.multisubdownloader.serviceproviders;

import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.TvSubtitlesAdapter;

@NullMarked
public class TvSubtitlesServiceProvider implements ServiceProvider {

    @Override
    public Supplier<TvSubtitlesAdapter> createProviderSupplier(UserInteractionHandler userInteractionHandler) {
        return () -> new TvSubtitlesAdapter(userInteractionHandler);
    }
}
