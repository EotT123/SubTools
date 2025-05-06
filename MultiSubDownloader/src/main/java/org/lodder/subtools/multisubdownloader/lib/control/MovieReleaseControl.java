package org.lodder.subtools.multisubdownloader.lib.control;

import org.apache.commons.lang3.StringUtils;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.omdb.OmdbAdapter;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.ProviderIdType;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MovieReleaseControl extends ReleaseControl<MovieRelease> {
    private final OmdbAdapter omdbAdapter;

    private static final Logger LOGGER = LoggerFactory.getLogger(MovieReleaseControl.class);

    public MovieReleaseControl(Settings settings, Manager manager, UserInteractionHandler userInteractionHandler) {
        super(settings, manager, userInteractionHandler);
        this.omdbAdapter = OmdbAdapter.getInstance(manager, userInteractionHandler);
    }

    @Override
    public MovieRelease process(MovieRelease release) throws ReleaseControlException {
        if (StringUtils.isBlank(release.name)) {
            throw new ReleaseControlException("Unable to extract title, check file", release);
        }
        LOGGER.debug("process: movie [{}]", release.name);
        setImdbId(release);
        setTvdbId(release);
        processInfo(release);
        return release;
    }

    private void setImdbId(MovieRelease release) {
        release.providerIds.getImdbId().ifNotPresent(() -> omdbAdapter.searchMovie(release.name)
            .ifPresent(omdbRelease -> release.providerIds.add(ProviderIdType.IMDB, omdbRelease.imdbID)));
        release.providerIds.getImdbId().ifNotPresent(() -> imdbAdapter.getImdbId(release.name)
            .ifPresent(imdbId -> release.providerIds.add(ProviderIdType.IMDB, imdbId)));
        release.providerIds.getImdbId().ifNotPresent(() -> tvdbAdapter.searchMovie(release.name)
            .ifPresent(movie -> release.providerIds.add(ProviderIdType.IMDB, movie.imdbId)));
        if (release.providerIds.getImdbId().isEmpty()) {
            throw new IllegalStateException("Unable to find IMDB id for movie: " + release.name);
        }
    }

    private void setTvdbId(MovieRelease release) {
        release.providerIds.getTvdbId().ifNotPresent(() -> tvdbAdapter.searchMovie(release.name)
            .ifPresent(movie -> release.providerIds.add(ProviderIdType.TVDB, movie.id)));
        // TODO enable this, also use imdbId if present
//        release.providerIds.getTvdbId().ifNotPresent(() -> imdbAdapter.getSerieDetails(release.name)
//            .ifPresent(imdbDetails -> release.providerIds.add(ProviderIdType.TVDB, imdbDetails.tvdbId)));
    }

    private void processInfo(MovieRelease release) {
        release.providerIds.getImdbId().ifPresentOrElse(
            imdbId -> imdbAdapter.getMovieDetails(imdbId).ifPresent(details -> {
                release.year = details.year;
                release.name = details.name;
            }),
            () -> omdbAdapter.searchMovie(release.name).ifPresent(omdbRelease -> {
                try {
                    release.year = Integer.parseInt(omdbRelease.year);
                } catch (NumberFormatException e) {
                    // continue
                }
                release.name = omdbRelease.title;
            }));
    }

}
