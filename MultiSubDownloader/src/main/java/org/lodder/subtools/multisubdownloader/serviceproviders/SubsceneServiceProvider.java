package org.lodder.subtools.multisubdownloader.serviceproviders;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.Container;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.SubsceneAdapter;

public class SubsceneServiceProvider implements ServiceProvider {

    protected Container app;
    protected SubtitleProvider subtitleProvider;
    /* We define a priority lower than SubtitleServiceProvider */
    @val @override int priority = 1;

    @Override
    public void register(Container app, UserInteractionHandler userInteractionHandler) {
        /* Add the SubtitleProvider to the store */
        app.makeSubtitleProviderStore().addProvider(new SubsceneAdapter(app.makeManager(), userInteractionHandler));
    }
}
