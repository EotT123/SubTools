package org.lodder.subtools.multisubdownloader.serviceproviders;

import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.OpenSubAdapter;
import org.lodder.subtools.sublibrary.Credentials;

@NullMarked
public class OpenSubtitlesServiceProvider implements ServiceProvider {

    @Override
    public Supplier<OpenSubAdapter> createProviderSupplier(UserInteractionHandler userInteractionHandler) {
        return () -> {
            Credentials credentials = null;
            if (SettingsControl.settings.loginOpenSubtitlesEnabled) {
                String username = StringUtils.trimToNull(SettingsControl.settings.loginOpenSubtitlesUsername);
                String password = StringUtils.trimToNull(SettingsControl.settings.loginOpenSubtitlesPassword);
                /* Protect against empty login */
                if (username != null && password != null) {
                    credentials = new Credentials(username, password);
                }
            }
            return new OpenSubAdapter(credentials, userInteractionHandler);
        };
    }
}
