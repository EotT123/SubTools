package org.lodder.subtools.multisubdownloader.serviceproviders;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.Container;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;
import org.lodder.subtools.multisubdownloader.subtitleproviders.adapters.JAddic7edAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.adapters.JAddic7edViaProxyAdapter;
import org.lodder.subtools.sublibrary.Manager;

public class Addic7edServiceProvider implements ServiceProvider {

    protected Container app;
    protected SubtitleProvider subtitleProvider;
    /* We define a priority lower than SubtitleServiceProvider */
    @val @override int priority = 1;

    @Override
    public void register(Container app, UserInteractionHandler userInteractionHandler) {
        this.app = app;

        /* Resolve the SubtitleProviderStore from the IoC Container */
        final SubtitleProviderStore subtitleProviderStore = app.makeSubtitleProviderStore();

        /* Create the SubtitleProvider */
        subtitleProvider = createProvider(userInteractionHandler);

        /* Add the SubtitleProvider to the store */
        subtitleProviderStore.addProvider(subtitleProvider);

        /* Listen for settings-change event */
        this.registerListener(subtitleProviderStore, userInteractionHandler);
    }

    private SubtitleProvider createProvider(UserInteractionHandler userInteractionHandler) {
        Settings settings = app.makeSettings();
        Manager manager = app.makeManager();

        boolean loginEnabled = false;
        String username = "";
        String password = "";
        if (settings.loginAddic7edEnabled) {
            username = StringUtils.trim(settings.loginAddic7edUsername);
            password = StringUtils.trim(settings.loginAddic7edPassword);
            /* Protect against empty login */
            loginEnabled = !username.isEmpty() && !password.isEmpty();
        }

        if (settings.serieSourceAddic7edProxy) {
            return new JAddic7edViaProxyAdapter(manager, userInteractionHandler);
        } else {
            return new JAddic7edAdapter(loginEnabled, username, password,
                app.makePreferences().getBoolean("speedy", false),
                manager, userInteractionHandler);
        }
    }

    // TODO is this still needed?
    private void registerListener(SubtitleProviderStore subtitleProviderStore,
        UserInteractionHandler userInteractionHandler) {

        /* Listen for settings-change */
        app.makeEventEmitter().listen("providers.settings.change", _ -> {
            /* Change occurred, delete outdated provider from store */
            subtitleProviderStore.deleteProvider(subtitleProvider);

            /* Re-create subtitle provider */
            subtitleProvider = createProvider(userInteractionHandler);

            /* Re-add provider to store */
            subtitleProviderStore.addProvider(subtitleProvider);
        });
    }
}
