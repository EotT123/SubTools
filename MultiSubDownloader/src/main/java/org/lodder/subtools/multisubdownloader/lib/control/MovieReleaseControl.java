package org.lodder.subtools.multisubdownloader.lib.control;

import static org.lodder.subtools.sublibrary.model.ProviderIdType.*;
import static util.Utils.*;

import com.tvdb.model.MovieBaseRecord;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.model.MovieReleaseWithoutPath;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public final class MovieReleaseControl extends ReleaseControl<MovieReleaseWithoutPath> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MovieReleaseControl.class);

    public MovieReleaseControl(UserInteractionHandler userInteractionHandler) {
        super(userInteractionHandler);
    }

    @Override
    public <R extends MovieReleaseWithoutPath> R process(R release) throws ReleaseControlException {
        if (StringUtils.isBlank(release.name)) {
            throw new ReleaseControlException("Unable to extract title, check file", release);
        }
        LOGGER.debug("process: movie [{}]", release.name);
        setImdbId(release);
        setTvdbId(release);
        processInfo(release);
        return release;
    }

    private void setImdbId(MovieReleaseWithoutPath release) {
        release.providerIds.getOrPut(IMDB, () -> omdbAdapter.searchMovie(release.name), r -> r.imdbID);
        release.providerIds.getOrPut(IMDB, () -> imdbAdapter.getImdbId(release.name, VideoType.MOVIE));
        //release.providerIds.getOrPut(IMDB, () ->tvdbAdapter.searchMovie(release.name), MovieBaseRecord::getImdbId);
        if (release.providerIds.get(IMDB) == null) {
            throw new IllegalStateException("Unable to find IMDB id for movie: " + release.name);
        }
    }

    private void setTvdbId(MovieReleaseWithoutPath release) {
        release.providerIds.getOrPut(TVDB, () -> tvdbAdapter.searchMovie(release.name), MovieBaseRecord::getId,
            Long::intValue);
        // TODO enable this, also use imdbId if present
        //release.providerIds.getOrPut(TVDB, () ->  imdbAdapter.getSerieDetails(release.name), imdbDetails::getTvdbId);
    }

    private void processInfo(MovieReleaseWithoutPath release) {
        String imdbId = release.providerIds.get(IMDB);
        if (imdbId != null) {
            ifNotNullDo(imdbAdapter.getDetails(imdbId), details -> {
                release.year = details.year;
                release.name = details.name;
            });
        } else {
            ifNotNullDo(omdbAdapter.searchMovie(release.name), omdbRelease -> {
                try {
                    release.year = Integer.parseInt(omdbRelease.year);
                } catch (NumberFormatException e) {
                    // continue
                }
                release.name = omdbRelease.title;
            });
        }
    }
}
