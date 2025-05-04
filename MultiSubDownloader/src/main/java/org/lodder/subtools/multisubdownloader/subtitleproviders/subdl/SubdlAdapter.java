package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import lombok.Getter;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.exception.SubdlException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSubtitle;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSubtitleMetadata;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception.SubsceneException;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public final class SubdlAdapter extends SubtitleAdapter<SubdlSubtitleMetadata, SubdlSubtitle, SubdlId, SubdlId,
    SubdlException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubdlAdapter.class);

    private static SubdlApi api;
    @val @override SubtitleSource source = SubtitleSource.SUBDL;
    @val @override boolean useSeasonForSerieId = false;

    public SubdlAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        if (api == null) {
            api = new SubdlApi(manager);
        }
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    @Override
    public Collection<SubdlSubtitleMetadata> searchMovieSubtitlesWithHash(String hash, Language language)
        throws SubdlException {
        return List.of();
    }

    @Override
    public Collection<SubdlSubtitleMetadata> searchMovieSubtitlesWithId(ProviderIds providerIds, Language language)
        throws SubdlException {
        return providerIds.getImdbId().mapThrowing(imdbId -> api.getMovieSubtitles(imdbId, language)).orElse(List.of());
    }

    @Override
    public Collection<SubdlSubtitleMetadata> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language) throws SubdlException {
        return api.getMovieSubtitles(name, language, year);
    }

    @Override
    public List<SubdlId> getSortedMovieProviderIds(ProviderIds providerIds, String title,
        @Nullable Integer year) throws SubsceneException {
        return api.getMovieProviderIds(title)
            .entrySet()
            .stream()
            .sorted(Comparator.comparingInt(entry -> providerTypeFunction.applyAsInt(entry.getKey())))
            .map(Entry::getValue)
            .flatMap(values -> values.stream().sorted(Comparator.comparing(s -> s.getScore(title, year))))
            .distinct()
            .toList();
    }

    @Override
    public String providerMovieIdToDisplayString(SubdlId providerSerieId) {
        return providerSerieId.name;
    }

    // ===== \\
    // SERIE \\
    // ===== \\

    @Override
    public Collection<SubdlSubtitleMetadata> searchSubtitles(SerieMapping serieMapping, int season, int episode,
        Language language) throws SubdlException {
        return api.getSerieSubtitles(serieMapping.providerId, season, episode, language);
    }


    @Override
    public SubdlSubtitle convertToSubtitle(SubdlSubtitleMetadata sub) {
        return new SubdlSubtitle(
            url:sub.url(),
            subtitleSource:source,
            fileName:sub.url.split("/").last().replace(".zip", ""),
            language:sub.language,
            quality:ReleaseParser.getQualityKeyword(sub.title + " " + sub.fileName),
            subtitleMatchType:SubtitleMatchType.EVERYTHING,
            releaseGroup:ReleaseParser.extractReleaseGroup(sub.title, sub.title.endsWith(".zip")),
            uploader:sub.uploader,
            hearingImpaired:sub.hearingImpaired);
    }


    public List<SubdlId> getSortedSerieProviderIds(ProviderIds providerIds, String serieName,
        @Nullable Integer season) throws SubdlException {

        List<SubdlId> subdlIds =
            providerIds.getImdbId().mapThrowing(imdbId -> api.getProviderIds(imdbId)).orElse(List.of())
                .elseIfEmptyThrowing(() -> api.getProviderIds(serieName));

        return subdlIds.stream().sorted(Comparator.comparing(
                (SubdlId n) -> !serieName.replaceAll("[^A-Za-z]", "")
                    .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", "")))
            .thenComparing(SubdlId::getYear, Comparator.nullsLast(Comparator.reverseOrder()))).toList();
    }

    @Override
    public String providerSerieIdToDisplayString(SubdlId SubdlSerieId) {
        return "${SubdlSerieId.name} (${SubdlSerieId.year} - ${SubdlSerieId.releaseType})";
    }
}
