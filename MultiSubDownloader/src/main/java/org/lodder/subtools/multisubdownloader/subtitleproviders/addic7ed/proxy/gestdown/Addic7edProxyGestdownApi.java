package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown;

import static manifold.science.measures.TimeUnit.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;
import static org.lodder.subtools.sublibrary.util.http.HttpStatus.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import extensions.java.lang.String.StringExt;
import jakarta.ws.rs.core.Response.Status.Family;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.gestdown.api.SubtitlesApi;
import org.gestdown.api.TvShowsApi;
import org.gestdown.model.EpisodeDto;
import org.gestdown.model.ShowDto;
import org.gestdown.model.SubtitleDto;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception.Addic7edException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception.Addic7edResponseException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.model.Addic7edProxyGestdownSubtitle;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.control.ReleaseParser.ReleaseParserExtraInfo;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.util.http.HttpStatus;
import org.opensubtitles.invoker.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Provides access to Addic7ed subtitle data via the Gestdown proxy.
 * <p>
 * Note: Only supports TV series search; movie support is not available.
 */
// see https://www.gestdown.info/Api
public class Addic7edProxyGestdownApi implements SubtitleApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(Addic7edProxyGestdownApi.class);

    private static final String DOMAIN = "https://api.gestdown.info";

    @val @override Manager manager;
    @val @override SubtitleSource source = SubtitleSource.ADDIC7ED;
    private static final TvShowsApi TV_SHOWS_API;
    private static final SubtitlesApi SUBTITLES_API;

    static {
        ApiClient apiClient = new ApiClient();
        TV_SHOWS_API = apiClient.createService(TvShowsApi.class);
        SUBTITLES_API = apiClient.createService(SubtitlesApi.class);
    }

    public Addic7edProxyGestdownApi(Manager manager) {
        this.manager = manager;
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    // No movie support

    // ===== \\
    // SERIE \\
    // ===== \\

    public List<ShowDto> getProviderSerieIds(String name) throws Addic7edException {
        return getCache("providerId", b -> b.add("name", name))
            .getCollection(() -> {
                List<ShowDto> shows = executeHandleStatus(() -> TV_SHOWS_API.showsSearchSearchGet(name)).getShows();
                return shows == null ? List.of() : shows;
//                return shows.stream().map(showDto -> new Addic7edProxyGestdownSerieId(showDto.name, showDto.id,
//                    showDto.tvDbId, showDto.tmdbId)).toList();
            });
    }

    public List<ShowDto> getProviderSerieIds(int tvdbId) throws Addic7edException {
        return getCache("providerId", b -> b.add("tvdbId", tvdbId))
            .getCollection(() -> {
                List<ShowDto> shows =
                    executeHandleStatus(() -> TV_SHOWS_API.showsExternalTvdbTvdbIdGet(tvdbId)).getShows();
                return shows == null ? List.of() : shows;
//                return shows.stream().map(showDto -> new Addic7edProxyGestdownSerieId(showDto.name, showDto.id,
//                    showDto.tvDbId, showDto.tmdbId)).toList();
            });
    }

    public List<SubtitleDto> getSubtitles(String providerId, int season, int episode,
        Language language) throws Addic7edException {
        return getCache("subtitles", b -> b.add("providerId", providerId)
            .add("season", season).add("episode", episode).add("language", language))
            .getCollection(() -> {
                List<SubtitleDto> subtitles =
                    executeHandleStatus(
                        () -> SUBTITLES_API.subtitlesGetShowUniqueIdSeasonEpisodeLanguageGet(language.getName(),
                            UUID.fromString(providerId), season, episode)).getMatchingSubtitles();
                return subtitles == null ? List.of() : subtitles.stream().filter(SubtitleDto::isCompleted).toList();

//                SubtitleSearchResponse response = SUBTITLES_API.subtitlesGetShowUniqueIdSeasonEpisodeLanguageGet(
//                    language.getName(), UUID.fromString(providerId), season, episode);
//                List<SubtitleDto> subtitles = response.getMatchingSubtitles();
//                if (subtitles == null || subtitles.isEmpty()) {
//                    return Set.of();
//                }
//                return subtitles
//                    .stream()
//                    .filter(SubtitleDto::isCompleted)
//                    .map(sub -> mapToSubtitle(sub, response.episode, language))
//                    .toSet();
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
            uploader:"",
            hearingImpaired:false);
    }

    private <T> T executeHandleStatus(ThrowingSupplier<Call<T>, IOException> callable, boolean retry=true)
        throws Addic7edException {
        Response<T> response = execute(() -> callable.get().execute());
        if (response.isSuccessful()) {
            return response.body();
        } else {
            String errorBody = execute(() -> Objects.requireNonNull(response.errorBody()).string());
            LOGGER.debug("Addic7ed error: " + errorBody);
            HttpStatus code = fromStatusCode(response.code());
            if (!retry) {
                throw new Addic7edResponseException(code);
            }
            if (code == TOO_MANY_REQUESTS) {
                sleep(5Second);
                return executeHandleStatus(callable, false);
            } else if (code.family == Family.SERVER_ERROR) {
                sleep(2Second);
                return executeHandleStatus(callable, false);
            } else {
                throw new Addic7edResponseException(code, errorBody);
            }
        }
    }

    public <T> T execute(ThrowingSupplier<T, IOException> supplier) throws Addic7edException {
        try {
            return supplier.get();
        } catch (IOException e) {
            throw new Addic7edException(e);
        }
    }
}
