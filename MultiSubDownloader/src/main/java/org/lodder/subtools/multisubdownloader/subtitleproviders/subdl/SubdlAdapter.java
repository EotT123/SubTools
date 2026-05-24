package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl;

import static org.lodder.subtools.sublibrary.model.ProviderIdType.*;
import static util.Utils.*;

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
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.ReleaseWithoutPath;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import subdl.Serie.ReleaseType;

@NullMarked
public final class SubdlAdapter extends
    SubtitleAdapter<SubdlSubtitleMetadata, SubdlSubtitle, SubdlSerieId, SubdlException> {

    private final SubdlApi api = new SubdlApi();
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.SUBDL;
    @val @override boolean useSeasonForSerieId = false;

    public SubdlAdapter(UserInteractionHandler userInteractionHandler) {
        super(userInteractionHandler);
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
        return providerIds.userOrElse(IMDB, imdbId -> api.getMovieSubtitles(imdbId, language), List::of);
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
        return providerIds.userOrElse(IMDB, imdbId -> ifNotNull(api.getProviderIdUsingImdbId(imdbId), List::of),
            List::of);
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
        return providerIds.userOrElse(IMDB,
            imdbId -> api.getSerieSubtitlesUsingImdbId(imdbId, season, episode, language),
            List::of);
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
        ReleaseWithoutPath releaseWithoutPath =
            ifNullThenGet(ReleaseParser.parse(sub.title), () -> ReleaseParser.parse(sub.fileName));
        return ifNotNullOrElseGet(releaseWithoutPath,
            release -> new SubdlSubtitle(
                sub.url,
                sub.title,
                sub.language,
                release.releaseGroup,
                sub.uploader,
                sub.hearingImpaired,
                release.quality,
                originalRelease),
            () -> new SubdlSubtitle(
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
