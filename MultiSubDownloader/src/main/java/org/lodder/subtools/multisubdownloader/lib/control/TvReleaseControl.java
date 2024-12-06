package org.lodder.subtools.multisubdownloader.lib.control;

import org.apache.commons.lang3.StringUtils;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.settings.model.SettingsProcessEpisodeSource;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.tvdb.TheTvdbAdapter;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TvReleaseControl extends ReleaseControl {

    private static final Logger LOGGER = LoggerFactory.getLogger(TvReleaseControl.class);

    private final TheTvdbAdapter jtvdba;
    private final TvRelease tvRelease;

    public TvReleaseControl(TvRelease tvRelease, Settings settings, Manager manager,
            UserInteractionHandler userInteractionHandler) {
        super(settings, manager);
        this.tvRelease = tvRelease;
        this.jtvdba = TheTvdbAdapter.getInstance(manager, userInteractionHandler);
    }

    @Override
    public void process() throws ReleaseControlException {
        if (StringUtils.isBlank(tvRelease.name)) {
            throw new ReleaseControlException("Unable to extract episode details, check file", tvRelease);
        } else {
            LOGGER.debug("process: show name [{}], season [{}], episode [{}]", tvRelease.name, tvRelease.season,
                    tvRelease.episodeNumbers);
            if (tvRelease.special) {
                processSpecial();
            } else {
                processTvdb();
            }
        }
    }

    private void processTvdb() throws ReleaseControlException {
        jtvdba.getSerie(tvRelease.name).useIfPresent(tvdbSerie -> {
            tvRelease.tvdbId = tvdbSerie.id;
            tvRelease.originalName = tvdbSerie.serieName;
            jtvdba.getEpisode(tvdbSerie.id, tvRelease.season, tvRelease.firstEpisodeNumber)
                    .useIfPresent(tvRelease::updateTvdbEpisodeInfo)
                    .orElseThrow(() -> new ReleaseControlException(
                            "Season ${tvRelease.season} Episode ${tvRelease.episodeNumbers} not found, check file",
                            tvRelease));
        }).orElseThrow(() -> new ReleaseControlException("Show not found, check file", tvRelease));
    }

    private void processSpecial() throws ReleaseControlException {
        jtvdba.getSerie(tvRelease.name).useIfPresent(tvdbSerie -> {
            tvRelease.tvdbId = tvdbSerie.id;
            tvRelease.originalName = tvdbSerie.serieName;
            if (getSettings().processEpisodeSource == SettingsProcessEpisodeSource.TVDB) {
                jtvdba.getEpisode(tvdbSerie.id, tvRelease.season, tvRelease.firstEpisodeNumber)
                        .ifPresent(tvRelease::updateTvdbEpisodeInfo);
            }
        }).orElseThrow(() -> new ReleaseControlException("Show not found, check file", tvRelease));
    }

    @Override
    public Release getVideoFile() {
        return tvRelease;
    }
}
