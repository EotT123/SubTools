package org.lodder.subtools.multisubdownloader.lib;

import java.nio.file.Path;
import java.util.Optional;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.lib.control.MovieReleaseControl;
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

@NullMarked
public record ReleaseFactory(Settings settings, Manager manager) {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseFactory.class);

    public Optional<Release> createRelease(Path file, UserInteractionHandler userInteractionHandler,
        boolean validate=true) {
        try {
            Optional<Release> release = ReleaseParser.parse(file);
            if (validate && release.isPresent()) {
                switch (release.get()) {
                    case TvRelease tvRelease ->
                        new TvReleaseControl(settings, manager, userInteractionHandler).process(tvRelease);
                    case MovieRelease movieRelease ->
                        new MovieReleaseControl(settings, manager, userInteractionHandler).process(movieRelease);
                }
            }
            return release;
        } catch (ReleaseParseException | ReleaseControlException e) {
            LOGGER.error("Failed to create a release for $file: " + e.getMessage(), e);
            return Optional.empty();
        }
    }
}
