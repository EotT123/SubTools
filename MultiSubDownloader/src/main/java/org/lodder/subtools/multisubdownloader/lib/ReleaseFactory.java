package org.lodder.subtools.multisubdownloader.lib;

import java.nio.file.Path;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.lib.control.MovieReleaseControl;
import org.lodder.subtools.multisubdownloader.lib.control.TvReleaseControl;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.model.MovieReleaseWithPath;
import org.lodder.subtools.sublibrary.model.MovieReleaseWithoutPath;
import org.lodder.subtools.sublibrary.model.ReleaseWithPath;
import org.lodder.subtools.sublibrary.model.ReleaseWithoutPath;
import org.lodder.subtools.sublibrary.model.TvReleaseWithPath;
import org.lodder.subtools.sublibrary.model.TvReleaseWithoutPath;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public record ReleaseFactory(Settings settings, Manager manager) {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseFactory.class);

    public @Nullable ReleaseWithPath createRelease(Path file,
        UserInteractionHandler userInteractionHandler, boolean validate=true) {
        try {
            ReleaseWithPath release = ReleaseParser.parse(file);
            if (validate && release != null) {
                switch (release) {
                    case TvReleaseWithPath tvRelease ->
                        new TvReleaseControl(settings, manager, userInteractionHandler).process(tvRelease);
                    case MovieReleaseWithPath movieRelease ->
                        new MovieReleaseControl(settings, manager, userInteractionHandler).process(movieRelease);
                }
            }
            return release;
        } catch (ReleaseControlException e) {
            LOGGER.error("Failed to create a release for $file: " + e.getMessage(), e);
            return null;
        }
    }

    public @Nullable ReleaseWithoutPath createRelease(String name,
        UserInteractionHandler userInteractionHandler, boolean validate=true) {
        try {
            ReleaseWithoutPath release = ReleaseParser.parse(name);
            if (validate && release != null) {
                switch (release) {
                    case TvReleaseWithoutPath tvRelease ->
                        new TvReleaseControl(settings, manager, userInteractionHandler).process(tvRelease);
                    case MovieReleaseWithoutPath movieRelease ->
                        new MovieReleaseControl(settings, manager, userInteractionHandler).process(movieRelease);
                }
            }
            return release;
        } catch (ReleaseControlException e) {
            LOGGER.error("Failed to create a release for $name: " + e.getMessage(), e);
            return null;
        }
    }
}
