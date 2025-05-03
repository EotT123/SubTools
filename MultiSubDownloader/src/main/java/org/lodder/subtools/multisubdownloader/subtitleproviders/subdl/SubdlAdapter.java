package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
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
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public final class SubdlAdapter extends SubtitleAdapter<SubdlSubtitleMetadata, SubdlSubtitle, SubdlSerieId,
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

    @Override
    public Collection<SubdlSubtitleMetadata> searchMovieSubtitlesWithHash(String hash,
        Language language) throws SubdlException {
        return api.searchSubtitles().movieHash(hash).language(language).searchSubtitles().getData();
    }

    @Override
    public Collection<SubdlSubtitleMetadata> searchMovieSubtitlesWithId(int tvdbId,
        Language language) throws SubdlException {
        return api.searchSubtitles().imdbId(tvdbId).language(language).searchSubtitles().getData();
    }

    @Override
    public Collection<SubdlSubtitleMetadata> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language) throws SubdlException {
        return api.getMovieSubtitles().query(name).language(language).searchSubtitles().getData();
    }

    @Override
    public Set<SubdlSubtitleMetadata> searchSerieSubtitles(TvRelease tvRelease,
        Language language) throws SubdlException {
        return getProviderSerieId(tvRelease).map(providerSerieId -> tvRelease.episodes.stream()
            .flatMap(episode -> {
                try {
                    return api.getSerieSubtitles(providerSerieId.providerId, tvRelease.season, episode, language)
                        .stream();
                } catch (SubdlException e) {
                    LOGGER.error("API $name searchSubtitles for serie [%s] (%s)".formatted(
                            TvRelease.formatName(providerSerieId.providerName, tvRelease.season, episode), e.getMessage()),
                        e);
                    return Stream.empty();
                }
            }).collect(Collectors.toSet())).orElseGet(Set::of);
    }


    @Override
    public SubdlSubtitle convertToSubtitle(SubdlSubtitleMetadata sub, Language language) {
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

    @Override
    public List<SubdlSerieId> getSortedProviderSerieIds(@Nullable Integer tvdbId, @Nullable Integer imdbId,
        String serieName, int season) throws SubdlException {
        return api.getProviderIds(imdbId, serieName).stream().sorted(Comparator.comparing(
                (SubdlSerieId n) -> !serieName.replaceAll("[^A-Za-z]", "")
                    .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", "")))
            .thenComparing(SubdlSerieId::getYear, Comparator.nullsLast(Comparator.reverseOrder()))).toList();
    }

    @Override
    public String providerSerieIdToDisplayString(SubdlSerieId SubdlSerieId) {
        return "${SubdlSerieId.name} (${SubdlSerieId.year} - ${SubdlSerieId.releaseType})";
    }
}
