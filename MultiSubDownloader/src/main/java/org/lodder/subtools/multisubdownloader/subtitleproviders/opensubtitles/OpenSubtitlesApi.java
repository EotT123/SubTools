package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles;

import static manifold.science.measures.TimeUnit.*;
import static manifold.science.util.UnitConstants.*;
import static org.lodder.subtools.sublibrary.LogLevel.*;
import static org.lodder.subtools.sublibrary.util.http.HttpStatus.*;
import static org.lodder.subtools.sublibrary.util.http.RetrofitService.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.NullMarked;
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
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.Manager.Retry;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.util.http.HttpClientException;
import org.lodder.subtools.sublibrary.util.http.HttpStatus;
import org.lodder.subtools.sublibrary.util.http.RetrofitService;
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

@NullMarked
public class OpenSubtitlesApi implements SubtitleApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenSubtitlesApi.class);

    private static final String USER_AGENT = "SubTools v1.0";
    private static final String CONTENT_TYPE = "application/json";
    private static final String APIKEY = "YrrY0zddovN1rY55tCWQbMxcNR68wnN3";

    @val @override Manager manager;
    @val @override SubtitleSource source = SubtitleSource.OPENSUBTITLES;
    @Nullable Credentials credentials;

    private static final Function<Chain, Builder> DEFAULT_BUILDER =
        chain -> chain.request().newBuilder()
            .header("Api-Key", APIKEY)
            .header("Content-Type", CONTENT_TYPE)
            .header("User-Agent", USER_AGENT);

    private final LazySupplier<ApiClient> apiClient = new LazySupplier<>(() -> {
//        ApiKeyAuth apiKeyAuth = new ApiKeyAuth("header", "Api-Key");
//        apiKeyAuth.setApiKey(APIKEY);
        return new ApiClient(new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Builder builder = DEFAULT_BUILDER.apply(chain);
                if (credentials != null) {
                    try {
                        String bearerToken = getBearerToken(credentials.username, credentials.password);
                        builder.header("Authorization", "Bearer " + bearerToken);
                    } catch (OpenSubtitleException e) {
                        LOGGER.error(e.getMessage(), e.getCause());
                    }
                }
                return chain.proceed(builder.build());
            })
            .build());
    });
    private final LazySupplier<SubtitlesApi> subtitlesApi =
        new LazySupplier<>(() -> apiClient.get().createService(SubtitlesApi.class));
    private final LazySupplier<DownloadApi> downloadApi =
        new LazySupplier<>(() -> apiClient.get().createService(DownloadApi.class));

    public OpenSubtitlesApi(Manager manager, @Nullable Credentials credentials=null) throws OpenSubtitleException {
        this.manager = manager;
        this.credentials = credentials;
    }

    public static boolean isValidCredentials(String userName, String password) {
        try {
            getBearerTokenWithoutCache(userName, password);
            return true;
        } catch (OpenSubtitleException e) {
            return false;
        }
    }

    private String getBearerToken(String username, String password) throws OpenSubtitleException {
        return manager.getCache(CacheType.DISK, new CacheKeyBuilder("opensubtitles", "bearerToken"))
            .get(supplier:() -> getBearerTokenWithoutCache(username, password)
                .orElseThrow(() -> new OpenSubtitleException("Invalid username/password combination")),
                timeToLive:23.5hr);
    }

    private static Optional<String> getBearerTokenWithoutCache(String username, String password)
        throws OpenSubtitleException {
        try {
            return Optional.ofNullable(
                new ApiClient(new OkHttpClient.Builder()
                    .addInterceptor(chain -> chain.proceed(DEFAULT_BUILDER.apply(chain).build()))
                    .build()).createService(AuthenticationApi.class)
                    .login(CONTENT_TYPE, USER_AGENT, new LoginRequest().username(username).password(password))
                    .execute().body()).map(Login200Response::getToken);
        } catch (IOException e) {
            throw new OpenSubtitleException("Could not acquire a bearer token", e);
        }
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
                .add("year", year)).getCollection(() -> {
            Integer imdbIdInt = StringUtils.isNotBlank(imdbId) ? Integer.parseInt(imdbId.replace("tt", "")) : null;
            return apiCall(
                () -> subtitlesApi.get().subtitles(id, imdbIdInt, tmdbId, getValue(type), query,
                    language != null ? language.iso639_1 : null, movieHash, userId, getValue(hearingImpaired),
                    getValue(foreignPartsOnly), getValue(trustedSources), getValue(machineTranslated),
                    getValue(aiTranslated), orderBy == null ? null : orderBy.paramName, getValue(orderDirection),
                    parentFeatureId, parentImdbId, parentTmdbId, season, episode, year, getValue(movieHashMatch), page,
                    USER_AGENT))
                .addErrorHandler(UNAUTHORIZED, retry:false, logLevel:WARN)
                .addErrorHandler(FORBIDDEN, retry:false)
                .addErrorHandler(NOT_ACCEPTABLE, retry:false)
                .addErrorHandler(TOO_MANY_REQUESTS, retry:true, sleepTimeBeforeRetry:5Second, logLevel:WARN)
                .execute().getData();


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
                apiCall(() -> downloadApi.get().download(USER_AGENT,
                    new DownloadRequest().fileId(fileId)))
                    .addErrorHandler(createQuotaErrorHandler())
                    .addErrorHandler(UNAUTHORIZED, retry:false, logLevel:WARN)
                    .addErrorHandler(FORBIDDEN, retry:false)
                    .addErrorHandler(NOT_ACCEPTABLE, retry:false)
                    .addErrorHandler(TOO_MANY_REQUESTS, retry:true, sleepTimeBeforeRetry:5Second, logLevel:WARN)
                    .execute().getLink()
            );
    }

    private static <T> ExecuteCall<T, OpenSubtitleResponseException> apiCall(
        ThrowingSupplier<Call<T>, OpenSubtitleResponseException> supplier) {
        return RetrofitService.handleExecution(supplier, OpenSubtitleResponseException::new);
    }


    private @Nullable String getValue(@Nullable ParamIntf param) {
        return param == null ? null : param.value;
    }

    private ErrorHandler<OpenSubtitleResponseException> createQuotaErrorHandler() {
        return new ErrorHandler<>(
            (HttpStatus code, String errorBody) -> code == NOT_ACCEPTABLE && errorBody.contains("quota"),
            retry:false,
            exception:(HttpStatus code, String errorBody) -> {
                try {
                    return new OpenSubtitleResponseException(code, new JSONObject(errorBody).getString("message"),
                        true);
                } catch (JSONException e) {
                    return new OpenSubtitleResponseException(code, "Quota exceeded. Please " + "try again later.",
                        true);
                }
            });
    }
}
