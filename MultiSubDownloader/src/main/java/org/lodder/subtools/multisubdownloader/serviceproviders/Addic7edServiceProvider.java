package org.lodder.subtools.multisubdownloader.serviceproviders;

import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.Addic7edAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.Addic7edProxyGestdownAdapter;
import org.lodder.subtools.sublibrary.Credentials;

@NullMarked
public class Addic7edServiceProvider implements ServiceProvider {

    @Override
    public Supplier<SubtitleProvider> createProviderSupplier(UserInteractionHandler userInteractionHandler) {
        return () -> {
            boolean loginEnabled = false;
            Credentials credentials = null;
            if (SettingsControl.settings.loginAddic7edEnabled) {
                String username = StringUtils.trimToNull(SettingsControl.settings.loginAddic7edUsername);
                String password = StringUtils.trimToNull(SettingsControl.settings.loginAddic7edPassword);
                /* Protect against empty login */
                if (username != null && password != null) {
                    credentials = new Credentials(username, password);
                }
            }

            if (SettingsControl.settings.serieSourceAddic7edProxy) {
                return new Addic7edProxyGestdownAdapter(userInteractionHandler);
            } else {
                return new Addic7edAdapter(credentials, userInteractionHandler);
            }
        };
    }
}