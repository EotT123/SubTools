package org.lodder.subtools.multisubdownloader.subtitleproviders.adapters;

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
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.SubdlApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.exception.SubDlException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSerieId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSubtitleMetadata;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.util.lazy.LazySupplier;
import org.opensubtitles.model.SubtitleAttributes;
import org.opensubtitles.model.SubtitleAttributesFilesInner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public final class JSubDlAdapter extends AbstractAdapter<SubdlSubtitleMetadata, SubdlSerieId, SubDlException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSubDlAdapter.class);

    private static LazySupplier<SubdlApi> api;
    @val @override SubtitleSource subtitleSource = SubtitleSource.SUBDL;
    @val @override String providerName = subtitleSource.name();
    @val @override boolean useSeasonForSerieId = false;

    public JSubDlAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        if (api == null) {
            api = new LazySupplier<>(() -> new SubdlApi(manager));
        }
    }

    @Override
    public List<Subtitle> searchMovieSubtitlesWithHash(String hash, Language language) throws SubDlException {
        return api.searchSubtitles().movieHash(hash).language(language).searchSubtitles().getData();
    }

    @Override
    public List<Subtitle> searchMovieSubtitlesWithId(int tvdbId, Language language) throws SubDlException {
        return api.searchSubtitles().imdbId(tvdbId).language(language).searchSubtitles().getData();
    }

    @Override public Collection<Subtitle> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language) throws SubDlException {
        return api.searchSubtitles().query(name).language(language).searchSubtitles().getData();
    }

    @Override
    public Set<Subtitle> convertToSubtitles(MovieRelease movieRelease, Set<Subtitle> subtitles, Language language) {
        return subtitles.stream().map(Subtitle::getAttributes).filter(
                attributes -> attributes.getFeatureDetails().getYear() != null ?
                    Objects.equals(attributes.getFeatureDetails().getYear().intValue(), movieRelease.year) :
                    movieRelease.year == null)
            .flatMap(attributes -> attributes.getFiles().stream().map(file -> createSubtitle(file, attributes)))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<SubdlSubtitleMetadata> searchSerieSubtitles(TvRelease tvRelease,
        Language language) throws SubDlException {
        return getProviderSerieId(tvRelease).map(providerSerieId -> tvRelease.episodes.stream()
            .flatMap(episode -> {
                try {
                    return api.get().getSubtitles(providerSerieId.providerId, tvRelease.season, episode, language)
                        .stream();
                } catch (SubDlException e) {
                    LOGGER.error("API %s searchSubtitles for serie [%s] (%s)".formatted(subtitleSource.name,
                            TvRelease.formatName(providerSerieId.providerName, tvRelease.season, episode), e.getMessage()),
                        e);
                    return Stream.empty();
                }
            }).collect(Collectors.toSet())).orElseGet(Set::of);
    }


    @Override
    public Set<Subtitle> convertToSubtitles(TvRelease tvRelease, Collection<SubdlSubtitleMetadata> subtitles,
        Language language) {
        String name = StringUtils.lowerCase(RegExUtils.replaceAll(tvRelease.name, "[^A-Za-z]", ""));
        String originalName = StringUtils.lowerCase(RegExUtils.replaceAll(tvRelease.originalName, "[^A-Za-z]", ""));
        return subtitles.stream().map(sub ->
                new Subtitle(
                    downloadSource:Subtitle.DownloadSource.of(sub.url()),
                    subtitleSource:subtitleSource,
                    fileName:sub.url(),
                    language:Language.fromIdOptional(
                        attributes.getLanguage()).orElse(null),
                    quality:ReleaseParser.getQualityKeyword(
                        file.getFileName()),
                    subtitleMatchType:SubtitleMatchType.EVERYTHING,
                    releaseGroup:ReleaseParser.extractReleaseGroup(file.fileName, file.fileName.endsWith(".srt")),
                    uploader:attributes.getUploader() != null ? attributes.getUploader().getName() : null,
                    hearingImpaired:Boolean.TRUE == attributes.isHearingImpaired()))
            .collect(Collectors.toSet());
    }

    private Subtitle createSubtitle(SubtitleAttributesFilesInner file, SubtitleAttributes attributes) {
        return new Subtitle(
            downloadSource:Subtitle.DownloadSource.of(
                () -> api.downloadSubtitle().fileId(file.getFileId().intValue()).download()
                    .getLink()), subtitleSource:subtitleSource, fileName:file.getFileName(), language:Language.fromIdOptional(
            attributes.getLanguage()).orElse(null), quality:ReleaseParser.getQualityKeyword(
            file.getFileName()), subtitleMatchType:SubtitleMatchType.EVERYTHING, releaseGroup:ReleaseParser.extractReleaseGroup(
            file.fileName, file.fileName.endsWith(".srt")), uploader:attributes.getUploader() != null ?
            attributes.getUploader().getName() : null, hearingImpaired:Boolean.TRUE == attributes.isHearingImpaired());
    }

    @Override
    public List<SubdlSerieId> getSortedProviderSerieIds(@Nullable Integer tvdbId, @Nullable Integer imdbId,
        String serieName, int season) throws SubDlException {
        return api.get().getProviderIds(imdbId, serieName).stream().sorted(Comparator.comparing(
                (SubdlSerieId n) -> !serieName.replaceAll("[^A-Za-z]", "")
                    .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", "")))
            .thenComparing(SubdlSerieId::getYear, Comparator.nullsLast(Comparator.reverseOrder()))).toList();
    }

    @Override
    public String providerSerieIdToDisplayString(SubdlSerieId SubdlSerieId) {
        return "${SubdlSerieId.name} (${SubdlSerieId.year} - ${SubdlSerieId.releaseType})";
    }
}
