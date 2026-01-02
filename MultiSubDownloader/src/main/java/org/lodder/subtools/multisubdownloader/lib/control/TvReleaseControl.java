package org.lodder.subtools.multisubdownloader.lib.control;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.model.ProviderIdType;
import org.lodder.subtools.sublibrary.model.TvReleaseWithoutPath;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public final class TvReleaseControl extends ReleaseControl<TvReleaseWithoutPath> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TvReleaseControl.class);

    public TvReleaseControl(Settings settings, Manager manager, UserInteractionHandler userInteractionHandler) {
        super(settings, manager, userInteractionHandler);
    }

    @Override
    public TvReleaseWithoutPath process(TvReleaseWithoutPath release) throws ReleaseControlException {
        if (StringUtils.isBlank(release.name)) {
            throw new ReleaseControlException("Unable to extract episode details, check file", release);
        }
        LOGGER.debug("process: serie [{}], season [{}], episode [{}]", release.name, release.season, release.episodes);
        setImdbId(release);
        setTvdbId(release);
        processTvdbInfo(release);
        processImdbInfo(release);
        return release;
    }

    private void setImdbId(TvReleaseWithoutPath release) {
        release.providerIds.getImdbId().ifNotPresent(() -> imdbAdapter.getImdbId(release.name, VideoType.EPISODE)
            .ifPresent(imdbId -> release.providerIds.add(ProviderIdType.IMDB, imdbId)));
        release.providerIds.getImdbId().ifNotPresent(() -> omdbAdapter.searchSerie(release.name)
            .ifPresent(omdbRelease -> release.providerIds.add(ProviderIdType.IMDB, omdbRelease.imdbID)));
//        release.providerIds.getImdbId().ifNotPresent(() -> tvdbAdapter.searchSerie(release.name)
//            .ifPresent(serie -> release.providerIds.add(ProviderIdType.IMDB, serie.imdbId)));
        if (release.providerIds.getImdbId().isEmpty()) {
            throw new IllegalStateException("Unable to find IMDB id for movie: " + release.name);
        }
    }

    private void setTvdbId(TvReleaseWithoutPath release) {
        release.providerIds.getTvdbId().ifNotPresent(() -> tvdbAdapter.searchSerie(release.name, release.providerIds)
            .map(serie -> serie.providerId)
            .ifPresent(tvdbId -> release.providerIds.add(ProviderIdType.TVDB, Integer.parseInt(tvdbId))));
        if (release.providerIds.getTvdbId().isEmpty()) {
//            throw new IllegalStateException("Unable to find TVDB id for movie: " + release.name);
        }
    }

    private void processTvdbInfo(TvReleaseWithoutPath release) {
        release.providerIds.getTvdbId()
            .flatMapToObj(tvdbId -> tvdbAdapter.searchEpisode(tvdbId, release.season, release.firstEpisode))
            .ifPresent(episode -> release.title = episode.name);
    }

    private void processImdbInfo(TvReleaseWithoutPath release) {
        // TODO implement this
//        release.providerIds.getImdbId().ifPresent(
//            imdbId -> imdbAdapter.getSerieDetails(imdbId)
//                .ifPresent(tvRelease::updateImdbEpisodeInfo));
    }
}
