package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitleException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model.OpenSubtilteSubtitle;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model.OpensubtitleSerieId;
import org.lodder.subtools.sublibrary.Credentials;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderInitException;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.opensubtitles.model.SubtitleAttributes;
import org.opensubtitles.model.SubtitleAttributesFilesInner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public final class OpenSubAdapter
    extends SubtitleAdapter<org.opensubtitles.model.Subtitle, OpenSubtilteSubtitle, OpensubtitleSerieId,
    OpenSubtitleException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenSubAdapter.class);

    private static OpenSubtitlesApi api;
    @val @override SubtitleSource source = SubtitleSource.OPENSUBTITLES;
    @val @override boolean useSeasonForSerieId = false;

    public OpenSubAdapter(Manager manager, Credentials credentials, UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        if (api == null) {
            try {
                api = new OpenSubtitlesApi(manager, credentials);
            } catch (OpenSubtitleException e) {
                throw new SubtitlesProviderInitException(name, e);
            }
        }
    }

    @Override
    public List<org.opensubtitles.model.Subtitle> searchMovieSubtitlesWithHash(String hash, Language language)
        throws OpenSubtitleException {
        return api.searchSubtitles(movieHash:hash, language:language);
    }

    @Override
    public List<org.opensubtitles.model.Subtitle> searchMovieSubtitlesWithId(int tvdbId, Language language)
        throws OpenSubtitleException {
        return api.searchSubtitles(imdbId:tvdbId, language:language);
    }

    @Override
    public Collection<org.opensubtitles.model.Subtitle> searchMovieSubtitlesWithName(String name,
        @Nullable Integer year, Language language) throws OpenSubtitleException {
        return api.searchSubtitles(query:name, language:language);
    }

    @Override
    public Set<Subtitle> convertToSubtitles(MovieRelease movieRelease, Set<org.opensubtitles.model.Subtitle> subtitles,
        Language language) {
        return subtitles.stream()
            .map(org.opensubtitles.model.Subtitle::getAttributes)
            .filter(attributes ->
                attributes.getFeatureDetails().getYear() != null
                    ? Objects.equals(attributes.getFeatureDetails().getYear().intValue(), movieRelease.year)
                    : movieRelease.year == null)
            .flatMap(attributes -> attributes.getFiles().stream().map(file -> createSubtitle(file, attributes)))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<org.opensubtitles.model.Subtitle> searchSerieSubtitles(TvRelease tvRelease, Language language)
        throws OpenSubtitleException {
        return getProviderSerieId(tvRelease).map(
            providerSerieId -> tvRelease.episodes.stream().flatMap(episode -> {
                try {
                    return api.searchSubtitles(
                        query:providerSerieId.name,
                        season:tvRelease.season,
                        episode:episode,
                        language:language)
                        .stream();
                } catch (OpenSubtitleException e) {
                    LOGGER.error("API $name searchSubtitles for serie [%s] (%s)".formatted(
                        TvRelease.formatName(providerSerieId.providerName, tvRelease.season, episode),
                        e.getMessage()), e);
                    return Stream.empty();
                }
            }).collect(Collectors.toSet())).orElseGet(Set::of);
    }

    @Override
    public OpenSubtilteSubtitle convertToSubtitle(org.opensubtitles.model.Subtitle sub, Language language) {


        String name = StringUtils.lowerCase(RegExUtils.replaceAll(tvRelease.name, "[^A-Za-z]", ""));
        String originalName = StringUtils.lowerCase(RegExUtils.replaceAll(tvRelease.originalName, "[^A-Za-z]", ""));
        return subtitles.stream()
            .map(org.opensubtitles.model.Subtitle::getAttributes)
            .flatMap(attributes -> attributes.getFiles().stream().filter(file -> {
                String subFileName = file.getFileName().replaceAll("[^A-Za-z]", "").toLowerCase();
                return subFileName.contains(name) ||
                    (StringUtils.isNotBlank(originalName) && subFileName.contains(originalName));
            }).map(file -> createSubtitle(file, attributes)))
            .collect(Collectors.toSet());
    }

    private Subtitle createSubtitle(SubtitleAttributesFilesInner file, SubtitleAttributes attributes) {
        return new OpenSubtilteSubtitle(
            urlSupplier:() -> api.getDownloadUrl(file.getFileId().intValue()),
            fileName:file.getFileName(),
            language:Language.fromIdOptional(attributes.getLanguage()).orElse(null),
            releaseGroup:ReleaseParser.extractReleaseGroup(file.fileName, file.fileName.endsWith(".srt")),
            uploader:attributes.getUploader() != null ? attributes.getUploader().getName() : null,
            subtitleMatchType:SubtitleMatchType.EVERYTHING,
            quality:ReleaseParser.getQualityKeyword(file.getFileName()),
            hearingImpaired:Boolean.TRUE == attributes.isHearingImpaired());
    }

    @Override
    public List<OpensubtitleSerieId> getSortedProviderSerieIds(@Nullable Integer tvdbId, @Nullable Integer imdbId,
        String serieName, int season) throws OpenSubtitleException {
        return api.getProviderSerieIds(serieName)
            .stream()
            .sorted(
                Comparator.comparing((OpensubtitleSerieId n) -> !serieName.replaceAll("[^A-Za-z]", "")
                        .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", "")))
                    .thenComparing(OpensubtitleSerieId::getYear, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    @Override
    public String providerSerieIdToDisplayString(OpensubtitleSerieId providerSerieId) {
        return "${providerSerieId.name} (${providerSerieId.year})";
    }
}
