package org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

public final class PodnapisiAdapter
    extends SubtitleAdapter<PodnapisiSubtitleMetadata, PodnapisiSubtitle, ProviderId, PodnapisiException> {

    private static PodnapisiApi api;
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.PODNAPISI;
    @val @override boolean useSeasonForSerieId = false;

    public PodnapisiAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        if (api == null) {
            api = new PodnapisiApi(manager, "JBierSubDownloader");
        }
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    @Override
    public List<PodnapisiSubtitleMetadata> searchMovieSubtitlesWithHash(String hash, Language language) {
        return List.of();
    }

    @Override
    public List<PodnapisiSubtitleMetadata> searchMovieSubtitlesWithId(ProviderIds providerIds, Language language) {
        return List.of();
    }

    @Override
    public Collection<PodnapisiSubtitleMetadata> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language, ProviderIds providerIds) throws PodnapisiException {
        return api.getMovieSubtitles(name, year, language, providerIds);
    }

    // ===== \\
    // SERIE \\
    // ===== \\


    public List<ProviderId> getSerieProviderIdById(ProviderIds providerIds, @Nullable Integer season)
        throws PodnapisiException {
        return List.of();
    }

    @Override
    public List<ProviderId> getSortedSerieProviderIds(String serieName, @Nullable Integer season)
        throws PodnapisiException {
        return api.getProviderIdsUsingName(serieName);
    }

    @Override
    public String providerSerieIdToDisplayString(ProviderId providerId) {
        return providerId.name;
    }

    @Override
    public Optional<Collection<PodnapisiSubtitleMetadata>> searchSubtitles(ProviderIds providerIds, int season,
        int episode, Language language) throws PodnapisiException {
        return providerIds.getImdbId()
            .mapEx(imdbId -> api.getSerieSubtitlesUsingImdbId(imdbId, season, episode, language));
    }

    @Override
    public Optional<Collection<PodnapisiSubtitleMetadata>> searchSubtitles(SerieMapping serieMapping, int season,
        int episode, Language language) throws PodnapisiException {
        return Optional.of(api.getSerieSubtitles(serieMapping.providerName, season, episode, language));
    }

    // ====== \\
    // COMMON \\
    // ====== \\

    @Override
    public PodnapisiSubtitle convertToSubtitle(PodnapisiSubtitleMetadata metadata) {
        return ReleaseParser.parse(metadata.releaseString)
            .map(release -> new PodnapisiSubtitle(
                url:metadata.url,
                fileName:metadata.releaseString,
                language:metadata.language,
                quality:release.quality,
                releaseGroup:release.releaseGroup,
                uploader:metadata.uploaderName,
                hearingImpaired:metadata.hearingImpaired))
            .orElseGet(() -> new PodnapisiSubtitle(
                url:metadata.url,
                fileName:metadata.releaseString,
                language:metadata.language,
                quality:ReleaseParser.getQualityKeyword(metadata.releaseString),
                releaseGroup:ReleaseParser.extractReleaseGroup(metadata.releaseString,
                    StringUtils.endsWith(metadata.releaseString, ".srt")),
                uploader:metadata.uploaderName,
                hearingImpaired:metadata.hearingImpaired));
    }
}
