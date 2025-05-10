package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles;

import static manifold.science.measures.TimeUnit.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;
import static org.lodder.subtools.sublibrary.util.http.HttpStatus.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import jakarta.ws.rs.core.Response.Status.Family;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitleException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitleResponseException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model.OpensubtitleId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.AiTranslatedEnum;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.ForeignPartsOnlyEnum;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.HearingImpairedEnum;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.MachineTranslatedEnum;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.MoviehashMatchEnum;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.OrderDirectionEnum;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.ParamIntf;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.SearchSubtitlesEnum;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.TrustedSourcesEnum;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param.TypeEnum;
import org.lodder.subtools.sublibrary.Credentials;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.Retry;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.util.http.HttpClientException;
import org.lodder.subtools.sublibrary.util.http.HttpStatus;
import org.lodder.subtools.sublibrary.util.lazy.LazySupplier;
import org.opensubtitles.api.AuthenticationApi;
import org.opensubtitles.api.DownloadApi;
import org.opensubtitles.api.SubtitlesApi;
import org.opensubtitles.invoker.ApiClient;
import org.opensubtitles.model.DownloadRequest;
import org.opensubtitles.model.Login200Response;
import org.opensubtitles.model.LoginRequest;
import org.opensubtitles.model.Subtitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

public class OpenSubtitlesApi implements SubtitleApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenSubtitlesApi.class);

    private static final String APIKEY = "3IlyaP0KNv6QmJ1gOBX8IXwzD1P9b8c0";//"lNNp0yv0ah8gytkmYPbHwuaATJqr4rS9";
    private static final ApiClient API_CLIENT;
    private static final LazySupplier<SubtitlesApi> SUBTITLES_API =
        new LazySupplier<>(() -> API_CLIENT.createService(SubtitlesApi.class));
    private static final LazySupplier<DownloadApi> DOWNLOAD_API =
        new LazySupplier<>(() -> API_CLIENT.createService(DownloadApi.class));
    private static final String USER_AGENT = "SubTools v1.0";
    @val @override Manager manager;
    @val @override SubtitleSource source = SubtitleSource.OPENSUBTITLES;

    static {
        API_CLIENT = new ApiClient();
        API_CLIENT.setApiKey(APIKEY);
    }

    public OpenSubtitlesApi(Manager manager, Credentials credentials=null) throws OpenSubtitleException {
        this.manager = manager;
        if (credentials != null) {
            login(credentials);
        } else {
            API_CLIENT.setBearerToken(null);
        }
        SUBTITLES_API.reset();
        DOWNLOAD_API.reset();
    }

    public void login(Credentials credentials) throws OpenSubtitleException {
        try {
            Login200Response response = login(credentials.username, credentials.password);
            API_CLIENT.setBearerToken(response.getToken());
        } catch (OpenSubtitleException e) {
            LOGGER.debug("OpenSubtitles: Login failed", e);
            throw new OpenSubtitleException(e);
        }
    }

    public static boolean isValidCredentials(String userName, String password) {
        try {
            login(userName, password);
            return true;
        } catch (OpenSubtitleException e) {
            return false;
        }
    }

    private static Login200Response login(String userName, String password)
        throws OpenSubtitleException {
        return executeHandleStatus(() -> API_CLIENT.createService(AuthenticationApi.class).login("application/json",
            USER_AGENT, new LoginRequest().username(userName).password(password)));
    }

    // ===== \\
    // MOVIE \\
    // ===== \\


    // ===== \\
    // SERIE \\
    // ===== \\

    public List<OpensubtitleId> getProviderSerieIds(String serieName) throws OpenSubtitleException {
        return getCache("providerSerieIds", b -> b.add("serieName", serieName))
            .getCollection(() -> {
                try {
                    return manager.getAsJsonArray(PageContentParams.params(
                            url:"https://www.opensubtitles.org/libs/suggest.php?format=json3&MovieName="
                                + URLEncoder.encode(serieName.toLowerCase(), StandardCharsets.UTF_8),
                            cacheType:CacheType.MEMORY,
                            userAgent:"",
                            retry:new Retry(
                                1,
                                exc -> exc instanceof HttpClientException e && e.responseCode == 429,
                                5Second)
                            ))
                        .streamJsonObjects()
                        .filter(show -> "tv".equals(show.getString("kind")))
                        .map(show -> new OpensubtitleId(show.getString("name"), show.getInt("id"),
                            show.getString("year")))
                        .toList();
                } catch (Exception e) {
                    throw new OpenSubtitleException(e);
                }
            });
    }

    // ====== \\
    // COMMON \\
    // ====== \\


    public List<Subtitle> searchSubtitles(
        @Nullable AiTranslatedEnum aiTranslated=AiTranslatedEnum.EXCLUDE,
        @Nullable Integer episode=null,
        @Nullable ForeignPartsOnlyEnum foreignPartsOnly=null,
        @Nullable HearingImpairedEnum hearingImpaired=null,
        @Nullable Integer id=null,
        @Nullable String imdbId=null,
        @Nullable Language language=null,
        @Nullable MachineTranslatedEnum machineTranslated=MachineTranslatedEnum.EXCLUDE,
        @Nullable String movieHash=null,
        @Nullable MoviehashMatchEnum movieHashMatch=null,
        @Nullable SearchSubtitlesEnum orderBy=null,
        @Nullable OrderDirectionEnum orderDirection=null,
        @Nullable Integer page=null,
        @Nullable Integer parentFeatureId=null,
        @Nullable Integer parentImdbId=null,
        @Nullable Integer parentTmdbId=null,
        @Nullable String query=null,
        @Nullable Integer season=null,
        @Nullable Integer tmdbId=null,
        @Nullable TrustedSourcesEnum trustedSources=null,
        @Nullable TypeEnum type=null,
        @Nullable Integer userId=null,
        @Nullable Integer year=null) throws OpenSubtitleException {

        return getCache("subtitles",
            b -> b
                .add("aiTranslated", aiTranslated)
                .add("episode", episode)
                .add("foreignPartsOnly", foreignPartsOnly)
                .add("hearingImpaired", hearingImpaired)
                .add("id", id)
                .add("imdbId", imdbId)
                .add("language", language)
                .add("machineTranslated", machineTranslated)
                .add("movieHash", movieHash)
                .add("movieHashMatch", movieHashMatch)
                .add("orderBy", orderBy)
                .add("orderDirection", orderDirection)
                .add("page", page)
                .add("parentFeatureId", parentFeatureId)
                .add("parentImdbId", parentImdbId)
                .add("parentTmdbId", parentTmdbId)
                .add("query", query)
                .add("season", season)
                .add("tmdbId", tmdbId)
                .add("trustedSources", trustedSources)
                .add("type", type)
                .add("userId", userId)
                .add("year", year))
            .getCollection(() -> {
                Integer imdbIdInt = StringUtils.isNotBlank(imdbId) ? Integer.parseInt(imdbId.replace("tt", "")) : null;
                return executeHandleStatus(
                    () -> SUBTITLES_API.get().subtitles(id, imdbIdInt, tmdbId, getValue(type), query,
                        language != null ? language.iso639_1 : null, movieHash, userId,
                        getValue(hearingImpaired), getValue(foreignPartsOnly), getValue(trustedSources),
                        getValue(machineTranslated), getValue(aiTranslated),
                        orderBy == null ? null : orderBy.paramName, getValue(orderDirection),
                        parentFeatureId, parentImdbId, parentTmdbId, season, episode, year,
                        getValue(movieHashMatch), page, USER_AGENT)).data;
                // TODO is this filtering needed?
                // String name = StringUtils.lowerCase(RegExUtils.replaceAll(tvRelease.name, "[^A-Za-z]", ""));
                // String originalName = StringUtils.lowerCase(RegExUtils.replaceAll(tvRelease.originalName, "[^A-Za-z]", ""));
                //     .filter(file -> {
                //     String subFileName = file.getFileName().replaceAll("[^A-Za-z]", "").toLowerCase();
                //     return subFileName.contains(name) ||
                //         (StringUtils.isNotBlank(originalName) && subFileName.contains(originalName));
                // })
            });
    }


    public String getDownloadUrl(int fileId) throws OpenSubtitleException {
        return getCache("downloadUrl", b -> b.add("fileId", fileId))
            .get(() ->
                executeHandleStatus(() -> DOWNLOAD_API.get().download(USER_AGENT,
                    new DownloadRequest().fileId(fileId))).link
            );
    }

    private static <T> T executeHandleStatus(ThrowingSupplier<Call<T>, IOException> callable, boolean retry=true)
        throws OpenSubtitleException {
        Response<T> response = execute(() -> callable.get().execute());
        if (response.isSuccessful()) {
            return response.body();
        } else {
            String errorBody = execute(() -> Objects.requireNonNull(response.errorBody()).string());
            LOGGER.debug("OpenSubtitle error: " + errorBody);
            HttpStatus code = fromStatusCode(response.code());
            if (!retry) {
                throw new OpenSubtitleResponseException(code);
            }
            if (code == TOO_MANY_REQUESTS) {
                sleep(5Second);
                return executeHandleStatus(callable, false);
            } else if (code.family == Family.SERVER_ERROR) {
                sleep(2Second);
                return executeHandleStatus(callable, false);
            } else if (code == NOT_ACCEPTABLE && errorBody.contains("Your quota will be renewed")) {
                throw new OpenSubtitleResponseException(code, new JSONObject(errorBody).getString("message"), true);
            } else {
                throw new OpenSubtitleResponseException(code, errorBody);
            }
        }
    }

    public static <T> T execute(ThrowingSupplier<T, IOException> supplier) throws OpenSubtitleException {
        try {
            return supplier.get();
        } catch (IOException e) {
            throw new OpenSubtitleException(e);
        }
    }

    private String getValue(ParamIntf param) {
        return param == null ? null : param.value;
    }
}
