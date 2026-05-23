package org.lodder.subtools.multisubdownloader.serviceproviders;

import java.util.function.Supplier;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.Container;
import org.lodder.subtools.multisubdownloader.framework.service.providers.ServiceProvider;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.OpenSubAdapter;
import org.lodder.subtools.sublibrary.Credentials;

@NullMarked
public class OpenSubtitlesServiceProvider implements ServiceProvider {

    /* We define a priority lower than SubtitleServiceProvider */
    @val @override int priority = 1;

    @Override
    public Supplier<OpenSubAdapter> createProviderSupplier(Container app,
        UserInteractionHandler userInteractionHandler) {
        return () -> {
            Settings settings = app.makeSettings();

            Credentials credentials = null;
            if (settings.loginOpenSubtitlesEnabled) {
                String username = StringUtils.trimToNull(settings.loginOpenSubtitlesUsername);
                String password = StringUtils.trimToNull(settings.loginOpenSubtitlesPassword);
                /* Protect against empty login */
                if (username != null && password != null) {
                    credentials = new Credentials(username, password);
                }
            }
            return new OpenSubAdapter(app.makeManager(), credentials, userInteractionHandler);
        };
    }
}
