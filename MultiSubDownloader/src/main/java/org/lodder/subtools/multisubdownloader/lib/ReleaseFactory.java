package org.lodder.subtools.multisubdownloader.lib;

import java.nio.file.Path;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.lib.control.MovieReleaseControl;
import org.lodder.subtools.multisubdownloader.lib.control.TvReleaseControl;
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
public record ReleaseFactory() {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseFactory.class);

    public @Nullable ReleaseWithPath createRelease(Path file, UserInteractionHandler userInteractionHandler) {
        try {
            return switch (ReleaseParser.parse(file)) {
                case TvReleaseWithPath r -> new TvReleaseControl(userInteractionHandler).process(r);
                case MovieReleaseWithPath r -> new MovieReleaseControl(userInteractionHandler).process(r);
                case null -> null;
            };
        } catch (ReleaseControlException e) {
            LOGGER.error("Failed to create a release for $file: " + e.getMessage(), e);
            return null;
        }
    }

    public @Nullable ReleaseWithoutPath createRelease(String name, UserInteractionHandler userInteractionHandler,
        boolean process=true) {
        try {
            ReleaseWithoutPath release = ReleaseParser.parse(name);
            if (process) {
                return switch (release) {
                    case TvReleaseWithoutPath r -> new TvReleaseControl(userInteractionHandler).process(r);
                    case MovieReleaseWithoutPath r -> new MovieReleaseControl(userInteractionHandler).process(r);
                    case null -> null;
                };
            }
            return release;
        } catch (ReleaseControlException e) {
            LOGGER.error("Failed to create a release for $name: " + e.getMessage(), e);
            return null;
        }
    }
}
