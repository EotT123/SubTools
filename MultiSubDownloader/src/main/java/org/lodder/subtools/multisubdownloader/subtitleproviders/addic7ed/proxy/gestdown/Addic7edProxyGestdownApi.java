package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown;

import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static org.lodder.subtools.sublibrary.util.http.RetrofitService.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import extensions.java.lang.String.StringExt;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.gestdown.api.SubtitlesApi;
import org.gestdown.api.TvShowsApi;
import org.gestdown.invoker.ApiClient;
import org.gestdown.model.EpisodeDto;
import org.gestdown.model.ShowDto;
import org.gestdown.model.SubtitleDto;
import org.gestdown.model.SubtitleSearchResponse;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception.Addic7edApiException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.model.Addic7edProxyGestdownSubtitle;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.control.ReleaseParser.ReleaseParserExtraInfo;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.util.http.RetrofitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

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

    @val @override Manager manager;
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.ADDIC7ED_GESTDOWN;
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

    public List<ShowDto> getProviderSerieIds(String name) throws Addic7edApiException {
        return getCache("providerId", b -> b.add("name", name))
            .getCollection(
                () -> {
                    List<ShowDto> shows = apiCall(() -> TV_SHOWS_API.showsSearchSearchGet(name)).execute().getShows();
                    if (shows == null) {
                        throw Addic7edApiException.noResult("Serie [$name] not found");
                    }
                    return shows;
                }
            );
    }

    public Optional<ShowDto> getProviderSerieIds(int tvdbId) throws Addic7edApiException {
        return getCache("providerId", b -> b.add("tvdbId", tvdbId))
            .getOptional(() -> {
                List<ShowDto> shows =
                    apiCall(() -> TV_SHOWS_API.showsExternalTvdbTvdbIdGet(tvdbId)).execute().getShows();
                if (shows == null) {
                    return Optional.empty();
                }
                return shows.stream().findFirst();
            });
    }

    public List<Addic7edProxyGestdownSubtitle> getSubtitles(String providerId, int season, int episode,
        Language language) throws Addic7edApiException {
        return getCache("subtitles", b -> b.add("providerId", providerId)
            .add("season", season).add("episode", episode).add("language", language))
            .getCollection(() -> {
                SubtitleSearchResponse response = apiCall(
                    () -> SUBTITLES_API.subtitlesGetShowUniqueIdSeasonEpisodeLanguageGet(language.iso639_3,
                        UUID.fromString(providerId), season, episode)).execute();
                List<SubtitleDto> subtitles = response.getMatchingSubtitles();
                if (subtitles == null) {
                    throw Addic7edApiException.noResult("Could not find subtitles for [$providerId], season[$season]," +
                        " episode [$episode], language [$language]", CACHE_DISABLED);
                }
                return subtitles.stream().map(subtitleDto -> mapToSubtitle(subtitleDto, response.episode, language))
                    .toList();
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

    private static <T> ExecuteCall<T, Addic7edApiException> apiCall(
        ThrowingSupplier<Call<T>, Addic7edApiException> supplier) {
        return RetrofitService.handleExecution(supplier, Addic7edApiException::new);
    }
}
