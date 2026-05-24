package org.lodder.subtools.multisubdownloader.serviceproviders;

import java.util.function.Supplier;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.Container;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.local.Local;

@NullMarked
public class LocalServiceProvider implements ServiceProvider {

    /* We define a priority lower than SubtitleServiceProvider */
    @val @override int priority = 1;

    @Override
    public Supplier<Local> createProviderSupplier(Container app, UserInteractionHandler userInteractionHandler) {
        return () -> new Local(app.makeManager(), userInteractionHandler);
    }
}
