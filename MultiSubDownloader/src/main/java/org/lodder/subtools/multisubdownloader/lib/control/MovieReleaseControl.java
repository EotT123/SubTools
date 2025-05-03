package org.lodder.subtools.multisubdownloader.lib.control;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.omdb.OmdbAdapter;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.ReleaseIdType;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MovieReleaseControl extends ReleaseControl {
    private final OmdbAdapter omdbAdapter;
    private final MovieRelease movieRelease;
    @val @override Release release;

    private static final Logger LOGGER = LoggerFactory.getLogger(MovieReleaseControl.class);

    public MovieReleaseControl(MovieRelease movieRelease, Settings settings, Manager manager,
        UserInteractionHandler userInteractionHandler) {
        super(settings, manager, userInteractionHandler);
        this.movieRelease = movieRelease;
        this.release = movieRelease;
        this.omdbAdapter = OmdbAdapter.getInstance(manager, userInteractionHandler);
    }

    @Override
    public void process() throws ReleaseControlException {
        if (StringUtils.isBlank(movieRelease.name)) {
            throw new ReleaseControlException("Unable to extract title, check file", movieRelease);
        }
        LOGGER.debug("process: movie [{}]", movieRelease.name);
        setImdbId();
        setTvdbId();
        processInfo();
    }

    private void setImdbId() {
        release.releaseIds.getImdbId().ifNotPresent(() -> omdbAdapter.searchMovie(release.name)
            .ifPresent(omdbRelease -> release.releaseIds.add(ReleaseIdType.IMDB, omdbRelease.imdbID)));
        release.releaseIds.getImdbId().ifNotPresent(() -> imdbAdapter.getImdbId(release.name)
            .ifPresent(imdbId -> release.releaseIds.add(ReleaseIdType.IMDB, imdbId)));
        release.releaseIds.getImdbId().ifNotPresent(() -> tvdbAdapter.searchMovie(release.name)
            .ifPresent(movie -> release.releaseIds.add(ReleaseIdType.IMDB, movie.imdbId)));
    }

    private void setTvdbId() {
        release.releaseIds.getTvdbId().ifNotPresent(() -> tvdbAdapter.searchMovie(release.name)
            .ifPresent(movie -> release.releaseIds.add(ReleaseIdType.TVDB, movie.id)));
        // TODO enable this, also use imdbId if present
//        release.releaseIds.getTvdbId().ifNotPresent(() -> imdbAdapter.getSerieDetails(release.name)
//            .ifPresent(imdbDetails -> release.releaseIds.add(ReleaseIdType.TVDB, imdbDetails.tvdbId)));
    }

    private void processInfo() {
        release.releaseIds.getImdbId().ifPresentOrElse(
            imdbId -> imdbAdapter.getMovieDetails(imdbId).ifPresent(details -> {
                movieRelease.year = details.year;
                movieRelease.name = details.name;
            }),
            () -> omdbAdapter.searchMovie(release.name).ifPresent(omdbRelease -> {
                movieRelease.year = omdbRelease.year;
                movieRelease.name = omdbRelease.title;
            }));
    }

}
