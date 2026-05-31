package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown;

import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static util.Utils.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import extensions.java.lang.String.StringExt;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.gestdown.api.SubtitlesApi;
import org.gestdown.api.TvShowsApi;
import org.gestdown.invoker.ApiClient;
import org.gestdown.model.EpisodeDto;
import org.gestdown.model.ShowDto;
import org.gestdown.model.ShowSearchResponse;
import org.gestdown.model.SubtitleDto;
import org.gestdown.model.SubtitleSearchResponse;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception.Addic7edApiException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.model.Addic7edProxyGestdownSubtitle;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.connection.retrofit.ErrorResponse;
import org.lodder.subtools.sublibrary.connection.retrofit.SuccessfulResponse;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.control.ReleaseParser.ReleaseParserExtraInfo;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to Addic7ed subtitle data via the Gestdown proxy.
 * <p>
 * Note: Only supports TV series search; movie support is not available.
 */
// see https://www.gestdown.info/Api
@NullMarked
public class Addic7edProxyGestdownApi implements SubtitleApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(Addic7edProxyGestdownApi.class);

    private static final String DOMAIN = "https://api.gestdown.info";

    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.ADDIC7ED_GESTDOWN;
    private static final TvShowsApi TV_SHOWS_API;
    private static final SubtitlesApi SUBTITLES_API;

    static {
        ApiClient apiClient = new ApiClient();
        TV_SHOWS_API = apiClient.createService(TvShowsApi.class);
        SUBTITLES_API = apiClient.createService(SubtitlesApi.class);
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    // No movie support

    // ===== \\
    // SERIE \\
    // ===== \\

    public List<ShowDto> getProviderSerieIds(String name) throws Addic7edApiException {
        return getCache("providerId", b -> b.add("name", name))
            .get(() -> switch (TV_SHOWS_API.showsSearchSearchGet(name).call()) {
                case SuccessfulResponse<ShowSearchResponse> response ->
                    ifNullThrow(response.body.shows, () -> Addic7edApiException.noResult("Serie [$name] not found"));
                case ErrorResponse r -> throw handleErrorResponse(r, "Serie [$name] not found");
            });
    }


    public @Nullable ShowDto getProviderSerieId(int tvdbId) throws Addic7edApiException {
        return getCache("providerId", b -> b.add("tvdbId", tvdbId))
            .get(() -> switch (TV_SHOWS_API.showsExternalTvdbTvdbIdGet(tvdbId).call()) {
                case SuccessfulResponse<ShowSearchResponse> response -> first(response.body.shows);
                case ErrorResponse r -> throw handleErrorResponse(r, "tvdbId [$tvdbId] not found");
            });
    }

    public List<Addic7edProxyGestdownSubtitle> getSubtitles(String providerId, int season, int episode,
        Language language) throws Addic7edApiException {

        return getCache("subtitles", b -> b.add("providerId", providerId)
            .add("season", season).add("episode", episode).add("language", language))
            .get(() -> switch (
                SUBTITLES_API.subtitlesGetShowUniqueIdSeasonEpisodeLanguageGet(language.iso639_3,
                    UUID.fromString(providerId), season, episode).call()) {
                case SuccessfulResponse<SubtitleSearchResponse> response ->
                    Optional.ofNullable(response.body.matchingSubtitles)
                        .map(s -> s.stream().map(sub -> mapToSubtitle(sub, response.body.episode, language)).toList())
                        .orElseThrow(() -> Addic7edApiException.noResult("Could not find subtitles for " +
                                "[$providerId], season [$season], episode [$episode], language [$language]",
                            CACHE_DISABLED));
                case ErrorResponse r -> throw handleErrorResponse(r, "Could not find subtitles for " +
                    "[$providerId], season[$season], episode [$episode], language [$language]");
            });
    }

    // ====== \\
    // COMMON \\
    // ====== \\


    private Addic7edProxyGestdownSubtitle mapToSubtitle(SubtitleDto sub, EpisodeDto episodedto, Language language) {
        ReleaseParserExtraInfo extraInfoParser = ReleaseParser.parseExtraInfo(sub.getVersion());
        return new Addic7edProxyGestdownSubtitle(
            url:DOMAIN + sub.getDownloadUri(),
            fileName:StringExt.removeIllegalFilenameChars("${episodedto.show} - ${episodedto.title} - ${sub.version}"),
            language:language,
            quality:extraInfoParser.getQualityKeyword(),
            releaseGroup:extraInfoParser.getReleaseGroupBestEffort(),
            hearingImpaired:false);
    }

    private <T> @Nullable T first(@Nullable List<T> list) {
        return list == null || list.isEmpty() ? null : list.getFirst();
    }

    private Addic7edApiException handleErrorResponse(ErrorResponse errorResponse, String message) {
        return new Addic7edApiException(errorResponse.code, message + " - " + errorResponse.message,
            errorResponse.cacheStrategy, errorResponse.logLevel);
    }
}
