package org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi;

import java.util.Collection;
import java.util.List;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.exception.PodnapisiException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.model.PodnapisiSubtitle;
import org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.model.PodnapisiSubtitleMetadata;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PodnapisiAdapter
    extends SubtitleAdapter<PodnapisiSubtitleMetadata, PodnapisiSubtitle, ProviderId, PodnapisiException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PodnapisiAdapter.class);

    private static PodnapisiApi api;
    @val @override SubtitleSource source = SubtitleSource.PODNAPISI;
    @val @override boolean useSeasonForSerieId = false;

    public PodnapisiAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        if (api == null) {
            api = new PodnapisiApi(manager, "JBierSubDownloader");
        }
    }

    @Override
    public List<PodnapisiSubtitleMetadata> searchMovieSubtitlesWithHash(String hash, Language language) {
        return List.of();
    }

    @Override
    public List<PodnapisiSubtitleMetadata> searchMovieSubtitlesWithId(int tvdbId, Language language) {
        return List.of();
    }

    @Override
    public Collection<PodnapisiSubtitleMetadata> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language) throws PodnapisiException {
        return api.getMovieSubtitles(name, year, 0, 0, language);
    }

    @Override
    public Collection<PodnapisiSubtitleMetadata> searchSubtitles(SerieMapping serieMapping, int season, int episode,
        Language language) throws PodnapisiException {
        return api.getSerieSubtitles(serieMapping.providerId, season, episode, language);
    }

    @Override
    public PodnapisiSubtitle convertToSubtitle(PodnapisiSubtitleMetadata metadata) {
        return new PodnapisiSubtitle(
            url:metadata.url,
            subtitleSource:source,
            fileName:metadata.releaseString,
            language:metadata.language,
            quality:ReleaseParser.getQualityKeyword(metadata.releaseString),
            subtitleMatchType:SubtitleMatchType.EVERYTHING,
            releaseGroup:ReleaseParser.extractReleaseGroup(metadata.releaseString,
                StringUtils.endsWith(metadata.releaseString, ".srt")),
            uploader:metadata.uploaderName,
            hearingImpaired:metadata.hearingImpaired);
    }

    @Override
    public List<ProviderId> getSortedProviderSerieIds(@Nullable Integer tvdbId, @Nullable Integer imdbId,
        String serieName, int season) throws PodnapisiException {
        return api.getProviderId(serieName).stream().toList();
    }

    @Override
    public String providerSerieIdToDisplayString(ProviderId providerId) {
        return providerId.name;
    }
}
