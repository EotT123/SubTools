package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles;

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
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitleException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model.OpenSubtilteSubtitle;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model.OpensubtitleId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.TypeEnum;
import org.lodder.subtools.sublibrary.Credentials;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.control.ReleaseParser.ReleaseParserExtraInfo;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderInitException;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.opensubtitles.model.Subtitle;
import org.opensubtitles.model.SubtitleAttributes;
import org.opensubtitles.model.SubtitleAttributesUploader;

@NullMarked
public final class OpenSubAdapter
    extends
    SubtitleAdapter<org.opensubtitles.model.Subtitle, OpenSubtilteSubtitle, OpensubtitleId, OpenSubtitleException> {

    private final OpenSubtitlesApi api;
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.OPENSUBTITLES;
    @val @override boolean useSeasonForSerieId = false;

    public OpenSubAdapter(Credentials credentials, UserInteractionHandler userInteractionHandler) {
        super(userInteractionHandler);
        try {
            api = new OpenSubtitlesApi(credentials);
        } catch (OpenSubtitleException e) {
            throw new SubtitlesProviderInitException(provider, e);
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
        return providerIds.userOrElse(IMDB,
            imdbId -> api.searchSubtitles(imdbId:imdbId, language:language, type:TypeEnum.MOVIE), List::of);
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
        return ifNotNull(providerIds.get(IMDB, api::getProviderSerieId), List::of);
    }

    @Override
    public List<OpensubtitleId> getSortedSerieProviderIds(String serieName, @Nullable Integer season)
        throws OpenSubtitleException {
        return api.getProviderSerieIds(serieName)
            .stream()
            .sorted(Comparator.comparing((OpensubtitleId n) -> !serieName.replaceAll("[^A-Za-z]", "")
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
        return providerIds.userOrElse(IMDB,
            imdbId -> api.searchSubtitles(
                imdbId:imdbId,
                season:season,
                episode:episode,
                language:language,
                type:TypeEnum.EPISODE),
            List::of);
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
        String uploader = ifNotNull(attr.getUploader(), SubtitleAttributesUploader::getName);
        int fileId = attr.files.stream().findFirst().orElseThrow().fileId.intValue();
        return ifNotNullOrElseGet(ReleaseParser.parse(attr.release),
            r -> new OpenSubtilteSubtitle(
                urlSupplier:() -> api.getDownloadUrl(fileId),
                fileName:ifNullThenGet(attr.release, release::getFileNameOrName),
                language:language,
                releaseGroup:r.releaseGroup,
                uploader:uploader,
                quality:r.quality,
                hearingImpaired:Boolean.TRUE == attr.isHearingImpaired()),
            () -> {
                ReleaseParserExtraInfo extraInfo = ReleaseParser.parseExtraInfo(attr.release);
                return new OpenSubtilteSubtitle(
                    urlSupplier:() -> api.getDownloadUrl(fileId),
                    fileName:ifNullThenGet(attr.release, release::getFileNameOrName),
                    language:language,
                    releaseGroup:extraInfo.getReleaseGroupBestEffort(),
                    uploader:uploader,
                    quality:extraInfo.qualityKeyword,
                    hearingImpaired:Boolean.TRUE == attr.isHearingImpaired());
            });
    }
}
