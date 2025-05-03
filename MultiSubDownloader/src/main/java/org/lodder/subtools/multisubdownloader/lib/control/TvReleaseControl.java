package org.lodder.subtools.multisubdownloader.lib.control;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.ReleaseIdType;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TvReleaseControl extends ReleaseControl {

    private static final Logger LOGGER = LoggerFactory.getLogger(TvReleaseControl.class);

    private final TvRelease tvRelease;
    @val @override Release release;

    public TvReleaseControl(TvRelease tvRelease, Settings settings, Manager manager,
        UserInteractionHandler userInteractionHandler) {
        super(settings, manager, userInteractionHandler);
        this.tvRelease = tvRelease;
        this.release = tvRelease;
    }

    @Override
    public void process() throws ReleaseControlException {
        if (StringUtils.isBlank(tvRelease.name)) {
            throw new ReleaseControlException("Unable to extract episode details, check file", tvRelease);
        }
        LOGGER.debug("process: serie [{}], season [{}], episode [{}]", tvRelease.name, tvRelease.season,
                tvRelease.episodes);
        setImdbId();
        setTvdbId();
        processTvdbInfo();
        processImdbInfo();
    }

    private void setImdbId() {
        release.releaseIds.getImdbId().ifNotPresent(() -> omdbAdapter.searchSerie(release.name)
            .ifPresent(omdbRelease -> release.releaseIds.add(ReleaseIdType.IMDB, omdbRelease.imdbID)));
        release.releaseIds.getImdbId().ifNotPresent(() -> imdbAdapter.getImdbId(release.name)
            .ifPresent(imdbId -> release.releaseIds.add(ReleaseIdType.IMDB, imdbId)));
        release.releaseIds.getImdbId().ifNotPresent(() -> tvdbAdapter.searchSerie(release.name)
            .ifPresent(serie -> release.releaseIds.add(ReleaseIdType.IMDB, serie.imdbId)));
        if (release.releaseIds.getImdbId().isEmpty()) {
            throw new IllegalStateException("Unable to find IMDB id for movie: " + release.name);
        }
    }

    private void setTvdbId() {
        release.releaseIds.getTvdbId().ifNotPresent(() -> tvdbAdapter.searchSerie(release.name)
            .ifPresent(serie -> release.releaseIds.add(ReleaseIdType.TVDB, serie.id)));
        // TODO enable this, also use imdbId if present
//        release.releaseIds.getTvdbId().ifNotPresent(() -> imdbAdapter.getSerieDetails(release.name)
//            .ifPresent(imdbDetails -> release.releaseIds.add(ReleaseIdType.TVDB, imdbDetails.tvdbId)));
        if (release.releaseIds.getTvdbId().isEmpty()) {
            throw new IllegalStateException("Unable to find TVDB id for movie: " + release.name);
        }
    }

    private void processTvdbInfo() {
        release.releaseIds.getTvdbId().ifPresent(
            tvdbId -> tvdbAdapter.searchEpisode(tvdbId, tvRelease.season, tvRelease.firstEpisode)
                .ifPresent(episode -> tvRelease.title = episode.episodeName));
    }

    private void processImdbInfo() {
        // TODO implement this
//        release.releaseIds.getImdbId().ifPresent(
//            imdbId -> imdbAdapter.getSerieDetails(imdbId)
//                .ifPresent(tvRelease::updateImdbEpisodeInfo));
    }
}
