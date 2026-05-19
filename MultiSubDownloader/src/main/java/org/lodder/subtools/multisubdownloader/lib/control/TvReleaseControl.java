package org.lodder.subtools.multisubdownloader.lib.control;

import static org.lodder.subtools.sublibrary.model.ProviderIdType.*;
import static util.Utils.*;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.tvdb.model.TvdbEpisode;
import org.lodder.subtools.sublibrary.data.tvdb.model.TvdbSerie;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
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
        release.providerIds.getOrPut(IMDB, () -> imdbAdapter.getImdbId(release.name, VideoType.EPISODE));
        release.providerIds.getOrPut(IMDB, () -> omdbAdapter.searchSerie(release.name), r -> r.imdbID);
        //release.providerIds.getOrPut(IMDB, () -> tvdbAdapter.searchSerie(release.name),  TvdbSerie::getImdbId);
        if (release.providerIds.get(IMDB) == null) {
            throw new IllegalStateException("Unable to find IMDB id for movie: " + release.name);
        }
    }

    private void setTvdbId(TvReleaseWithoutPath release) {
        release.providerIds.getOrPut(TVDB, () -> tvdbAdapter.searchSerie(release.name, release.providerIds),
            TvdbSerie::getProviderId, Integer::parseInt);
        if (release.providerIds.get(TVDB) == null) {
//            throw new IllegalStateException("Unable to find TVDB id for movie: " + release.name);
        }
    }

    private void processTvdbInfo(TvReleaseWithoutPath release) {
        release.title = release.providerIds.get(TVDB,
            tvdbId -> ifNotNull(tvdbAdapter.searchEpisode(tvdbId, release.season, release.firstEpisode),
                TvdbEpisode::name));
        ifNotNullDo(ifNotNull(tvdbAdapter.searchSerie(release.name, release.providerIds), TvdbSerie::getProviderName),
            v -> release.originalName = v);
    }

    private void processImdbInfo(TvReleaseWithoutPath release) {
        // TODO implement this
//        release.providerIds.getImdbId().ifPresent(
//            imdbId -> imdbAdapter.getSerieDetails(imdbId)
//                .ifPresent(tvRelease::updateImdbEpisodeInfo));
    }
}
