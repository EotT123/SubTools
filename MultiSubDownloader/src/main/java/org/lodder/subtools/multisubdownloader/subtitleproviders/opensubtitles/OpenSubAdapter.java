package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitleException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model.OpenSubtilteSubtitle;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model.OpensubtitleId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.TypeEnum;
import org.lodder.subtools.sublibrary.Credentials;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.control.ReleaseParser.ReleaseParserExtraInfo;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderInitException;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.opensubtitles.model.Subtitle;
import org.opensubtitles.model.SubtitleAttributes;

public final class OpenSubAdapter
    extends
    SubtitleAdapter<org.opensubtitles.model.Subtitle, OpenSubtilteSubtitle, OpensubtitleId, OpenSubtitleException> {

    private static OpenSubtitlesApi api;
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.OPENSUBTITLES;
    @val @override boolean useSeasonForSerieId = false;

    public OpenSubAdapter(Manager manager, Credentials credentials, UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        if (api == null) {
            try {
                api = new OpenSubtitlesApi(manager, credentials);
            } catch (OpenSubtitleException e) {
                throw new SubtitlesProviderInitException(provider, e);
            }
        }
    }

    @Override
    public List<org.opensubtitles.model.Subtitle> searchMovieSubtitlesWithHash(String hash, Language language)
        throws OpenSubtitleException {
        return api.searchSubtitles(movieHash:hash, language:language, type:TypeEnum.MOVIE);
    }

    @Override
    public List<org.opensubtitles.model.Subtitle> searchMovieSubtitlesWithId(ProviderIds providerIds, Language language)
        throws OpenSubtitleException {
        return providerIds.getImdbId()
            .mapEx(imdbId -> api.searchSubtitles(imdbId:imdbId, language:language, type:TypeEnum.MOVIE))
            .orElse(List.of());
    }

    @Override
    public Collection<org.opensubtitles.model.Subtitle> searchMovieSubtitlesWithName(String name,
        @Nullable Integer year, Language language, ProviderIds providerIds) throws OpenSubtitleException {
        return api.searchSubtitles(query:name, language:language, type:TypeEnum.MOVIE);
    }

    // ===== \\
    // SERIE \\
    // ===== \\

    @Override
    public List<OpensubtitleId> getSerieProviderIdById(ProviderIds providerIds, @Nullable Integer season)
        throws OpenSubtitleException {
        return List.of();
    }

    @Override
    public List<OpensubtitleId> getSortedSerieProviderIds(String serieName, @Nullable Integer season)
        throws OpenSubtitleException {
        return api.getProviderSerieIds(serieName)
            .stream()
            .sorted(
                Comparator.comparing((OpensubtitleId n) -> !serieName.replaceAll("[^A-Za-z]", "")
                        .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", "")))
                    .thenComparing(OpensubtitleId::getYear, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    @Override
    public String providerSerieIdToDisplayString(OpensubtitleId providerSerieId) {
        return "${providerSerieId.name} (${providerSerieId.year})";
    }

    @Override
    public Collection<Subtitle> searchSubtitles(ProviderIds providerIds, int season,
        int episode, Language language) throws OpenSubtitleException {
        return providerIds.getImdbId()
            .mapEx(imdbId ->
                api.searchSubtitles(
                    imdbId:imdbId,
                    season:season,
                    episode:episode,
                    language:language,
                    type:TypeEnum.EPISODE))
            .orElse(List.of());
    }

    @Override
    public Collection<org.opensubtitles.model.Subtitle> searchSubtitles(SerieMapping serieMapping, int season,
        int episode, Language language) throws OpenSubtitleException {
        return api.searchSubtitles(
            query:serieMapping.name,
            season:season,
            episode:episode,
            language:language,
            type:TypeEnum.EPISODE);
    }


    // ====== \\
    // COMMON \\
    // ====== \\

    @Override
    public OpenSubtilteSubtitle convertToSubtitle(Release release, org.opensubtitles.model.Subtitle sub) {
        SubtitleAttributes attr = sub.getAttributes();
        Language language = Language.ofIso639_1(attr.language);
        int fileId = attr.files.stream().findFirst().orElseThrow().fileId.intValue();
        return ReleaseParser.parse(attr.release)
            .map(r -> new OpenSubtilteSubtitle(
                urlSupplier:() -> api.getDownloadUrl(fileId),
                fileName:attr.release,
                language:language,
                releaseGroup:r.releaseGroup,
                uploader:attr.getUploader() != null ? attr.getUploader().getName() : null,
                quality:r.quality,
                hearingImpaired:Boolean.TRUE == attr.isHearingImpaired()))
            .orElseGet(() -> {
                ReleaseParserExtraInfo extraInfo = ReleaseParser.parseExtraInfo(attr.release);
                return new OpenSubtilteSubtitle(
                    urlSupplier:() -> api.getDownloadUrl(fileId),
                    fileName:attr.release,
                    language:Language.ofIso639_1(attr.language),
                    releaseGroup:extraInfo.getReleaseGroupBestEffort(),
                    uploader:attr.getUploader() != null ? attr.getUploader().getName() : null,
                    quality:extraInfo.qualityKeyword,
                    hearingImpaired:Boolean.TRUE == attr.isHearingImpaired());
            });
    }
}
