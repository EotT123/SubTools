package org.lodder.subtools.multisubdownloader.lib;

import java.nio.file.Path;

import org.lodder.subtools.multisubdownloader.lib.control.MovieReleaseControl;
import org.lodder.subtools.multisubdownloader.lib.control.ReleaseControl;
import org.lodder.subtools.multisubdownloader.lib.control.TvReleaseControl;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.exception.ReleaseParseException;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ReleaseFactory(Settings settings, Manager manager) {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseFactory.class);

    public Release createRelease(Path file, UserInteractionHandler userInteractionHandler, boolean validate=true) {
        try {
            ReleaseControl releaseControl = switch (ReleaseParser.parse(file)) {
                case TvRelease tvRelease -> new TvReleaseControl(tvRelease, settings, manager, userInteractionHandler);
                case MovieRelease movieRelease -> new MovieReleaseControl(movieRelease, settings, manager,
                    userInteractionHandler);
            };
            if (validate) {
                releaseControl.process();
            }
            return releaseControl.release;

        } catch (ReleaseParseException | ReleaseControlException e) {
            LOGGER.error("createRelease: " + e.getMessage(), e);
            return null;
        }
    }
}
