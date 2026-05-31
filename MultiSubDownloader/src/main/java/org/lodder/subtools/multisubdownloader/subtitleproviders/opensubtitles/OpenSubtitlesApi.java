package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles;

import static manifold.science.measures.TimeUnit.*;
import static manifold.science.util.UnitConstants.*;
import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static org.lodder.subtools.sublibrary.LogLevel.*;
import static org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus.*;
import static util.Utils.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import connection.retrofit.ErrorResponse;
import connection.retrofit.Response;
import connection.retrofit.Response.ErrorHandler;
import connection.retrofit.SuccessfulResponse;
import jakarta.ws.rs.core.MediaType;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitleApiException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitleException;
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
import org.lodder.subtools.sublibrary.LogLevel;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKey;
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.Manager.Retry;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.util.lazy.LazySupplier;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpClientException;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;
import org.opensubtitles.api.AuthenticationApi;
import org.opensubtitles.api.DownloadApi;
import org.opensubtitles.api.SubtitlesApi;
import org.opensubtitles.invoker.ApiClient;
import org.opensubtitles.model.Login200Response;
import org.opensubtitles.model.LoginRequest;
import org.opensubtitles.model.Subtitle;
import org.opensubtitles.model.Subtitles200Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class OpenSubtitlesApi implements SubtitleApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenSubtitlesApi.class);

    private static final String USER_AGENT = "SubTools v1.0";
    private static final String CONTENT_TYPE = "application/json";
    private static final String APIKEY = "YrrY0zddovN1rY55tCWQbMxcNR68wnN3";
    private static final ErrorHandler[] RETROFIT_ERROR_HANDLERS = {createQuotaErrorHandler()};
    private static final connection.http.Response.ErrorHandler[] HTTP_ERROR_HANDLERS =
        {createInvalidTokenErrorHandler()};

    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.OPENSUBTITLES;
    private @Nullable Credentials credentials;

    private static final Function<Chain, Builder> DEFAULT_BUILDER =
        chain -> chain.request().newBuilder()
            .header("Api-Key", APIKEY)
            .header("Content-Type", CONTENT_TYPE)
            .header("User-Agent", USER_AGENT);

    private final LazySupplier<ApiClient> apiClient = new LazySupplier<>(() ->
        new ApiClient(new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Builder builder = DEFAULT_BUILDER.apply(chain);
                if (credentials != null) {
                    try {
                        builder.header("Authorization",
                            "Bearer " + getBearerToken(credentials.username, credentials.password));
                    } catch (OpenSubtitleApiException e) {
                        // continue without authentication
                    }
                }
                return chain.proceed(builder.build());
            })
            .build()));
    private final LazySupplier<SubtitlesApi> subtitlesApi =
        new LazySupplier<>(() -> apiClient.get().createService(SubtitlesApi.class));
    private final LazySupplier<DownloadApi> downloadApi =
        new LazySupplier<>(() -> apiClient.get().createService(DownloadApi.class));

    public OpenSubtitlesApi(@Nullable Credentials credentials=null) {
        this.credentials = credentials;
    }

    public static boolean isValidCredentials(String userName, String password) {
        try {
            getBearerTokenWithoutCache(userName, password);
            return true;
        } catch (OpenSubtitleApiException e) {
            return false;
        }
    }

    private static void resetBearerToken() {
        getBearerTokenCache().remove();
    }

    private static String getBearerToken(String username, String password) throws OpenSubtitleApiException {
        return getBearerTokenCache().get(() -> getBearerTokenWithoutCache(username, password), 23.5hr);
    }

    private static CacheKey getBearerTokenCache() {
        return Manager.getCache(CacheType.DISK, new CacheKeyBuilder("opensubtitles", "bearerToken"));
    }

    private static String getBearerTokenWithoutCache(String username, String password) throws OpenSubtitleApiException {
        Response<Login200Response> response = new ApiClient(new OkHttpClient.Builder()
            .addInterceptor(chain -> chain.proceed(DEFAULT_BUILDER.apply(chain).build()))
            .build()).createService(AuthenticationApi.class)
            .login(CONTENT_TYPE, USER_AGENT, new LoginRequest().username(username).password(password))
            .call();
        return switch (response) {
            case SuccessfulResponse<Login200Response> r -> r.body.token;
            case ErrorResponse r -> throw new OpenSubtitleApiException(r.code, "Could not acquire a bearer token - "
                + r.message, r.cacheStrategy, r.logLevel);
        };
    }

    // ===== \\
    // MOVIE \\
    // ===== \\


    // ===== \\
    // SERIE \\
    // ===== \\

    public List<OpensubtitleId> getProviderSerieIds(String serieName) throws OpenSubtitleException {
        return getCache("providerSerieIds", b -> b.add("serieName", serieName))
            .get(() -> {
                try {
                    return Manager.getJsonArray(new PageContentParams(url:
                            "https://www.opensubtitles.org/libs/suggest.php?format=json3&MovieName="
                                + URLEncoder.encode(serieName.toLowerCase(), StandardCharsets.UTF_8),
                            cacheType:CacheType.MEMORY,
                            retry:new Retry(
                                1,
                                exc -> exc instanceof HttpClientException e && e.responseCode == 429,
                                5 Second), contentType:MediaType.APPLICATION_JSON
                            ))
                        .streamJsonObjects()
                        .filter(show -> "tv".equals(show.getString("kind")))
                        .map(show -> new OpensubtitleId(show.getString("name"), show.getInt("id"),
                            show.getString("year")))
                        .toList();
                } catch (Exception e) {
                    throw OpenSubtitleApiException.error(e,
                        "OpenSubtitlesApi: Error while retrieving provider serie id for [%s] - %s".formatted
                            (serieName, e.getCause()));
                }
            });
    }

    public @Nullable OpensubtitleId getProviderSerieId(String imdbId) throws OpenSubtitleException {
        return getCache("providerSerieId", b -> b.add("imdbId", imdbId))
            .get(() -> {
                try {
                    return Manager.getJsonArray(new PageContentParams(
                            url:"https://www.opensubtitles.org/libs/suggest.php?format=json3&MovieName=" + imdbId,
                            cacheType:CacheType.MEMORY,
                            retry:new Retry(
                                1,
                                exc -> exc instanceof HttpClientException e && e.responseCode == 429,
                                5 Second), contentType:MediaType.APPLICATION_JSON
                            ))
                        .streamJsonObjects()
                        .filter(show -> "tv".equals(show.getString("kind")))
                        .map(show -> new OpensubtitleId(show.getString("name"), show.getInt("id"),
                            show.getString("year")))
                        .findAny().orElse(null);
                } catch (Exception e) {
                    throw OpenSubtitleApiException.error(e,
                        "OpenSubtitlesApi: Error while retrieving provider serie id for imdbid [%s] - %s".formatted
                            (imdbId, e.getCause()));
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
        @Nullable Integer year=null) throws OpenSubtitleApiException {

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
            .get(() -> {
                Integer imdbIdInt = StringUtils.isNotBlank(imdbId) ? Integer.parseInt(imdbId.replace("tt", "")) : null;
                return switch (
                    subtitlesApi.get().subtitles(id, imdbIdInt, tmdbId, getValue(type), query,
                            language != null ? language.iso639_1 : null, movieHash, userId, getValue(hearingImpaired),
                            getValue(foreignPartsOnly), getValue(trustedSources), getValue(machineTranslated),
                            getValue(aiTranslated), orderBy == null ? null : orderBy.paramName,
                            getValue(orderDirection),
                            parentFeatureId, parentImdbId, parentTmdbId, season, episode, year,
                            getValue(movieHashMatch),
                            page, USER_AGENT)
                        .call(RETROFIT_ERROR_HANDLERS)) {
                    case SuccessfulResponse<Subtitles200Response> r -> r.body.data;
                    case ErrorResponse r -> {
                        StringBuilder sb = new StringBuilder("Could not find subtitles for:");
                        ifNotNullDo(id, v -> sb.append(" id[$v]"));
                        ifNotNullDo(imdbIdInt, v -> sb.append(" imdbIdInt[$v]"));
                        ifNotNullDo(tmdbId, v -> sb.append(" tmdbId[$v]"));
                        ifNotNullDo(getValue(type), v -> sb.append(" type[$v]"));
                        ifNotNullDo(query, v -> sb.append(" query[$v]"));
                        ifNotNullDo(language, v -> sb.append(" language[$v]"));
                        ifNotNullDo(movieHash, v -> sb.append(" movieHash[$v]"));
                        ifNotNullDo(userId, v -> sb.append(" userId[$v]"));
                        ifNotNullDo(hearingImpaired, v -> sb.append(" hearingImpaired[$v]"));
                        ifNotNullDo(getValue(foreignPartsOnly), v -> sb.append(" foreignPartsOnly[$v]"));
                        ifNotNullDo(getValue(trustedSources), v -> sb.append(" trustedSources[$v]"));
                        ifNotNullDo(getValue(machineTranslated), v -> sb.append(" machineTranslated[$v]"));
                        ifNotNullDo(getValue(aiTranslated), v -> sb.append(" aiTranslated[$v]"));
                        ifNotNullDo(orderBy, v -> sb.append(" orderBy[${v.paramName}]"));
                        ifNotNullDo(getValue(orderDirection), v -> sb.append(" direction[$v]"));
                        ifNotNullDo(parentFeatureId, v -> sb.append(" parentFeatureId[$v]"));
                        ifNotNullDo(parentImdbId, v -> sb.append(" parentImdbId[$v]"));
                        ifNotNullDo(parentTmdbId, v -> sb.append(" parentTmdbId[$v]"));
                        ifNotNullDo(season, v -> sb.append(" season[$v]"));
                        ifNotNullDo(episode, v -> sb.append(" episode[$v]"));
                        ifNotNullDo(year, v -> sb.append(" year[$v]"));
                        ifNotNullDo(getValue(movieHashMatch), v -> sb.append(" movieHashMatch[$v]"));
                        ifNotNullDo(page, v -> sb.append(" page[$v]"));
                        throw handleErrorResponse(r, sb.toString());
                    }
                };
                //// TODO is this filtering needed?
                //// String name = StringUtils.lowerCase(RegExUtils.replaceAll(tvRelease.name, "[^A-Za-z]", ""));
                //// String originalName = StringUtils.lowerCase(RegExUtils.replaceAll(tvRelease.originalName,
                //// "[^A-Za-z]", ""));
                ////     .filter(file -> {
                ////     String subFileName = file.getFileName().replaceAll("[^A-Za-z]", "").toLowerCase();
                ////     return subFileName.contains(name) ||
                ////         (StringUtils.isNotBlank(originalName) && subFileName.contains(originalName));
                //// })
            });
    }

    public String getDownloadUrl(int fileId) throws OpenSubtitleApiException {
        return getDownloadUrl(fileId, true);
    }


    private String getDownloadUrl(int fileId, boolean retryInvalidToken) throws OpenSubtitleApiException {
        return getCache("downloadUrl", b -> b.add("fileId", fileId))
            .get(() -> {
                try (HttpClient client = HttpClient.newHttpClient()) {

                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.opensubtitles.com/api/v1/download"))
                        .header("Accept", "application/json")
                        .header("Api-Key", APIKEY)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Test v1.0")
                        .addHeaderIfNotNull("Authorization",
                            ifNotNull(credentials, c -> "Bearer " + getBearerToken(c.username, c.password)))
                        .POST(HttpRequest.BodyPublishers.ofString(
                            new ObjectMapper().writeValueAsString(Map.of("file_id", fileId))))
                        .build();

                    return switch (client.call(request, HTTP_ERROR_HANDLERS)) {
                        case connection.http.SuccessfulResponse r ->
                            JsonParser.parseString(r.body).getAsJsonObject().get("link").getAsString();
                        case connection.http.ErrorResponse r ->
                            throw new OpenSubtitleApiException(r.code, r.message, CACHE_DISABLED, LogLevel.ERROR);
                    };
                } catch (IOException e) {
                    throw new OpenSubtitleApiException(SERVER_ERROR, e.getMessage(), CACHE_DISABLED, LogLevel.ERROR);
                }
            });
    }


    private @Nullable String getValue(@Nullable ParamIntf param) {
        return param == null ? null : param.value;
    }

    private static ErrorHandler createQuotaErrorHandler() {
        return new ErrorHandler(
            (HttpStatus code, String errorBody) -> code == NOT_ACCEPTABLE && errorBody.contains("quota"),
            null,
            (HttpStatus code, String errorBody) -> {
                String message;
                try {
                    message = new JSONObject(errorBody).getString("message");
                } catch (JSONException e) {
                    message = "Quota exceeded. Please try again later.";
                }
                return new ErrorResponse(code, message, WARN, CACHE_DISABLED);
            });
    }

    private static connection.http.Response.ErrorHandler createInvalidTokenErrorHandler() {
        return new connection.http.Response.ErrorHandler((_, body) -> body.contains("invalid token"),
            OpenSubtitlesApi::resetBearerToken);
    }

    private static OpenSubtitleApiException handleErrorResponse(ErrorResponse errorResponse, String message) {
        return new OpenSubtitleApiException(errorResponse.code, message + " - " + errorResponse.message,
            errorResponse.cacheStrategy, errorResponse.logLevel);
    }
}
