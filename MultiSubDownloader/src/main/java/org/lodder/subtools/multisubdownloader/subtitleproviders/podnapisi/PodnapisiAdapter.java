package org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi;

import static org.lodder.subtools.sublibrary.model.ProviderIdType.*;
import static util.Utils.*;

import java.util.Collection;
import java.util.List;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.exception.PodnapisiException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.model.PodnapisiSubtitle;
import org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.model.PodnapisiSubtitleMetadata;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

@NullMarked
public final class PodnapisiAdapter
    extends SubtitleAdapter<PodnapisiSubtitleMetadata, PodnapisiSubtitle, ProviderId, PodnapisiException> {

    private final PodnapisiApi api;
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.PODNAPISI;
    @val @override boolean useSeasonForSerieId = false;

    public PodnapisiAdapter(UserInteractionHandler userInteractionHandler) {
        super(userInteractionHandler);
        this.api = new PodnapisiApi("JBierSubDownloader");
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

    @Override
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
    public Collection<PodnapisiSubtitleMetadata> searchSubtitles(ProviderIds providerIds, int season,
        int episode, Language language) throws PodnapisiException {
        return providerIds.userOrElse(IMDB,
            imdbId -> api.getSerieSubtitlesUsingImdbId(imdbId, season, episode, language),
            List::of);
    }

    @Override
    public Collection<PodnapisiSubtitleMetadata> searchSubtitles(SerieMapping serieMapping, int season,
        int episode, Language language) throws PodnapisiException {
        return api.getSerieSubtitles(serieMapping.providerName, season, episode, language);
    }

    // ====== \\
    // COMMON \\
    // ====== \\

    @Override
    public PodnapisiSubtitle convertToSubtitle(Release release, PodnapisiSubtitleMetadata metadata) {
        return ifNotNullOrElseGet(ReleaseParser.parse(metadata.releaseString),
            r -> new PodnapisiSubtitle(
                url:metadata.url,
                fileName:metadata.releaseString,
                language:metadata.language,
                quality:r.quality,
                releaseGroup:r.releaseGroup,
                uploader:metadata.uploaderName,
                hearingImpaired:metadata.hearingImpaired),
            () -> new PodnapisiSubtitle(
                url:metadata.url,
                fileName:metadata.releaseString,
                language:metadata.language,
                quality:ReleaseParser.getQualityKeyword(metadata.releaseString),
                releaseGroup:ReleaseParser.extractReleaseGroup(metadata.releaseString,
                    Strings.CS.endsWith(metadata.releaseString, ".srt")),
                uploader:metadata.uploaderName,
                hearingImpaired:metadata.hearingImpaired));
    }
}
