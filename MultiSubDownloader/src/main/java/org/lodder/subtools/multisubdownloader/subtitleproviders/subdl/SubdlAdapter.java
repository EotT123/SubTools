package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.exception.SubdlException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSerieId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSubtitle;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSubtitleMetadata;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import subdl.Serie.ReleaseType;

@NullMarked
public final class SubdlAdapter extends
    SubtitleAdapter<SubdlSubtitleMetadata, SubdlSubtitle, SubdlSerieId, SubdlException> {

    private final SubdlApi api;
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.SUBDL;
    @val @override boolean useSeasonForSerieId = false;

    public SubdlAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        api = new SubdlApi(manager);
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    @Override
    public Collection<SubdlSubtitleMetadata> searchMovieSubtitlesWithHash(String hash, Language language) {
        return List.of();
    }

    @Override
    public Collection<SubdlSubtitleMetadata> searchMovieSubtitlesWithId(ProviderIds providerIds, Language language)
        throws SubdlException {
        return providerIds.getImdbId().mapEx(imdbId -> api.getMovieSubtitles(imdbId, language)).orElse(List.of());
    }

    @Override
    public Collection<SubdlSubtitleMetadata> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language, ProviderIds providerIds) throws SubdlException {
        return api.getMovieSubtitles(name, year, language);
    }

    // ===== \\
    // SERIE \\
    // ===== \\

    @Override
    public List<SubdlSerieId> getSerieProviderIdById(ProviderIds providerIds, @Nullable Integer season)
        throws SubdlException {
        return providerIds.getImdbId().flatMapEx(imdbId -> api.getProviderIdUsingImdbId(imdbId).map(List::of))
            .orElseGet(List::of);
    }

    @Override
    public List<SubdlSerieId> getSortedSerieProviderIds(String serieName, @Nullable Integer season)
        throws SubdlException {
        return api.getProviderIdsUsingSerieName(serieName).stream()
            .filter(serieId -> serieId.releaseType == ReleaseType.tv)
            .sorted(Comparator.comparing((SubdlSerieId serieId) -> serieId.calculateLevenshteinDistance(serieName))
                .thenComparing(SubdlSerieId::getYear, Comparator.nullsLast(Comparator.reverseOrder()))).toList();
    }

    @Override
    public String providerSerieIdToDisplayString(SubdlSerieId SubdlSerieId) {
        return "${SubdlSerieId.name} (${SubdlSerieId.year} - ${SubdlSerieId.releaseType})";
    }

    @Override
    public Collection<SubdlSubtitleMetadata> searchSubtitles(ProviderIds providerIds, int season, int episode,
        Language language) throws SubdlException {
        return providerIds.getImdbId().mapEx(imdbId -> api.getSerieSubtitlesUsingImdbId(imdbId, season, episode,
            language)).orElse(List.of());
    }

    @Override
    public Collection<SubdlSubtitleMetadata> searchSubtitles(SerieMapping serieMapping, int season,
        int episode, Language language) throws SubdlException {
        return api.getSerieSubtitles(serieMapping.providerId, season, episode, language);
    }

    // ====== \\
    // COMMON \\
    // ====== \\

    @Override
    public SubdlSubtitle convertToSubtitle(Release originalRelease, SubdlSubtitleMetadata sub) {
        return ReleaseParser.parse(sub.title).orElseMapEx(() -> ReleaseParser.parse(sub.fileName))
            .map(release -> new SubdlSubtitle(
                sub.url,
                sub.title,
                sub.language,
                release.releaseGroup,
                sub.uploader,
                sub.hearingImpaired,
                release.quality,
                originalRelease))
            .orElseGet(() -> new SubdlSubtitle(
                sub.url,
                sub.title,
                sub.language,
                ReleaseParser.extractReleaseGroup(sub.title, sub.title.endsWith(".zip")),
                sub.uploader,
                sub.hearingImpaired,
                ReleaseParser.getQualityKeyword(sub.title + " " + sub.fileName),
                originalRelease));
    }
}
