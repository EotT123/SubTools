package org.lodder.subtools.multisubdownloader.serviceproviders;

import java.util.function.Supplier;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.cli.CliOption;
import org.lodder.subtools.multisubdownloader.framework.Container;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.Addic7edAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.Addic7edProxyGestdownAdapter;
import org.lodder.subtools.sublibrary.Credentials;
import org.lodder.subtools.sublibrary.Manager;

@NullMarked
public class Addic7edServiceProvider implements ServiceProvider {

    /* We define a priority lower than SubtitleServiceProvider */
    @val @override int priority = 1;

    @Override
    public Supplier<SubtitleProvider> createProviderSupplier(Container app,
        UserInteractionHandler userInteractionHandler) {
        return () -> {
            Settings settings = app.makeSettings();
            Manager manager = app.makeManager();

            boolean loginEnabled = false;
            Credentials credentials = null;
            if (settings.loginAddic7edEnabled) {
                String username = StringUtils.trimToNull(settings.loginAddic7edUsername);
                String password = StringUtils.trimToNull(settings.loginAddic7edPassword);
                /* Protect against empty login */
                if (username != null && password != null) {
                    credentials = new Credentials(username, password);
                }
            }

            if (settings.serieSourceAddic7edProxy) {
                return new Addic7edProxyGestdownAdapter(manager, userInteractionHandler);
            } else {
                boolean speedy = app.makePreferences().getBoolean(CliOption.SPEEDY.value, false);
                return new Addic7edAdapter(manager, speedy, credentials, userInteractionHandler);
            }
        };
    }
}