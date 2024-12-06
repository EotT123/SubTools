package org.lodder.subtools.multisubdownloader.serviceproviders;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.Container;
import org.lodder.subtools.multisubdownloader.framework.event.Emitter;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.subtitleproviders.Local;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;
import org.lodder.subtools.sublibrary.Manager;

public class LocalServiceProvider implements ServiceProvider {

    private UserInteractionHandler userInteractionHandler;
    protected Container app;
    protected SubtitleProvider subtitleProvider;
    /* We define a priority lower than SubtitleServiceProvider */
    @val @override int priority = 1;

    //    @Override
    //    public int getPriority() {
    //        /* We define a priority lower than SubtitleServiceProvider */
    //        return 1;
    //    }

    @Override
    public void register(Container app, UserInteractionHandler userInteractionHandler) {
        this.app = app;
        this.userInteractionHandler = userInteractionHandler;

        /* Resolve the SubtitleProviderStore from the IoC Container */
        final SubtitleProviderStore subtitleProviderStore = (SubtitleProviderStore) app.make("SubtitleProviderStore");

        /* Create the SubtitleProvider */
        subtitleProvider = createProvider();

        /* Add the SubtitleProvider to the store */
        subtitleProviderStore.addProvider(subtitleProvider);

        /* Listen for settings-change event */
        this.registerListener(subtitleProviderStore);
    }

    private SubtitleProvider createProvider() {
        Settings settings = (Settings) this.app.make("Settings");
        Manager manager = (Manager) app.make("Manager");
        return new Local(settings, manager, userInteractionHandler);
    }

    private void registerListener(final SubtitleProviderStore subtitleProviderStore) {
        /* Resolve the EventEmitter from the IoC Container */
        Emitter emitter = (Emitter) app.make("EventEmitter");

        /* Listen for settings-change */
        emitter.listen("providers.settings.change", event -> {
            /* Change occurred, delete outdated provider from store */
            subtitleProviderStore.deleteProvider(subtitleProvider);

            /* Re-create subtitle provider */
            subtitleProvider = createProvider();

            /* Re-add provider to store */
            subtitleProviderStore.addProvider(subtitleProvider);
        });
    }
}
