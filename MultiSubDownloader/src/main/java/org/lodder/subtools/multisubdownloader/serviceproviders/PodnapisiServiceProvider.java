package org.lodder.subtools.multisubdownloader.serviceproviders;

import java.util.function.Supplier;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.Container;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.PodnapisiAdapter;

@NullMarked
public class PodnapisiServiceProvider implements ServiceProvider {
    /* We define a priority lower than SubtitleServiceProvider */
    @val @override int priority = 1;

    @Override
    public Supplier<PodnapisiAdapter> createProviderSupplier(Container app,
        UserInteractionHandler userInteractionHandler) {
        return () -> new PodnapisiAdapter(app.makeManager(), userInteractionHandler);
    }
}
