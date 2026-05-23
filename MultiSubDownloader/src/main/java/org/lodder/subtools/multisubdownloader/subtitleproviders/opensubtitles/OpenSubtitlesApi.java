package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles;

import static manifold.science.measures.TimeUnit.*;
import static manifold.science.util.UnitConstants.*;
import static org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus.*;
import static org.lodder.subtools.sublibrary.util.webpage.http.RetrofitService.*;
import static util.Utils.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import jakarta.ws.rs.core.MediaType;
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
import org.lodder.subtools.sublibrary.CacheStrategy;
import org.lodder.subtools.sublibrary.Credentials;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.LogLevel;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.Manager.Retry;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.util.lazy.LazySupplier;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpClientException;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;
import org.lodder.subtools.sublibrary.util.webpage.http.RetrofitService;
import org.opensubtitles.api.AuthenticationApi;
import org.opensubtitles.api.DownloadApi;
import org.opensubtitles.api.SubtitlesApi;
import org.opensubtitles.invoker.ApiClient;
import org.opensubtitles.model.Login200Response;
import org.opensubtitles.model.LoginRequest;
import org.opensubtitles.model.Subtitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

@NullMarked
public class OpenSubtitlesApi implements SubtitleApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenSubtitlesApi.class);

    private static final String USER_AGENT = "SubTools v1.0";
    private static final String CONTENT_TYPE = "application/json";
    private static final String APIKEY = "YrrY0zddovN1rY55tCWQbMxcNR68wnN3";

    @val @override Manager manager;
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
                        String bearerToken = getBearerToken(credentials.username, credentials.password);
                        builder.header("Authorization", "Bearer " + bearerToken);
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

    public OpenSubtitlesApi(Manager manager, @Nullable Credentials credentials=null) throws OpenSubtitleApiException {
        this.manager = manager;
        this.credentials = credentials;
    }

    public static boolean isValidCredentials(String userName, String password) {
        try {
            return getBearerTokenWithoutCache(userName, password) != null;
        } catch (OpenSubtitleApiException e) {
            return false;
        }
    }

    private String getBearerToken(String username, String password) throws OpenSubtitleApiException {
        return manager.getCache(CacheType.DISK, new CacheKeyBuilder("opensubtitles", "bearerToken"))
            .get(() -> ifNullThrow(getBearerTokenWithoutCache(username, password),
                () -> OpenSubtitleApiException.noResult(
                    "Could not acquire a bearer token, " + "invalid username/password?")), 23.5hr);
    }

    private static @Nullable String getBearerTokenWithoutCache(String username, String password)
        throws OpenSubtitleApiException {
        try {
            Response<Login200Response> response = new ApiClient(new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(DEFAULT_BUILDER.apply(chain).build()))
                .build()).createService(AuthenticationApi.class)
                .login(CONTENT_TYPE, USER_AGENT, new LoginRequest().username(username).password(password))
                .execute();
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            return response.body().getToken();
        } catch (IOException e) {
            return null;
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
            .get(() -> {
                try {
                    return manager.getJsonArray(new PageContentParams(
                            url:"https://www.opensubtitles.org/libs/suggest.php?format=json3&MovieName="
                                + URLEncoder.encode(serieName.toLowerCase(), StandardCharsets.UTF_8),
                            cacheType:CacheType.MEMORY,
                            retry:new Retry(
                                1,
                                exc -> exc instanceof HttpClientException e && e.responseCode == 429,
                                5Second), contentType:MediaType.APPLICATION_JSON
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
                    return manager.getJsonArray(new PageContentParams(
                            url:"https://www.opensubtitles.org/libs/suggest.php?format=json3&MovieName=" + imdbId,
                            cacheType:CacheType.MEMORY,
                            retry:new Retry(
                                1,
                                exc -> exc instanceof HttpClientException e && e.responseCode == 429,
                                5Second), contentType:MediaType.APPLICATION_JSON
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
                return apiCall(
                    () -> subtitlesApi.get().subtitles(id, imdbIdInt, tmdbId, getValue(type), query,
                        language != null ? language.iso639_1 : null, movieHash, userId, getValue(hearingImpaired),
                        getValue(foreignPartsOnly), getValue(trustedSources), getValue(machineTranslated),
                        getValue(aiTranslated), orderBy == null ? null : orderBy.paramName, getValue(orderDirection),
                        parentFeatureId, parentImdbId, parentTmdbId, season, episode, year, getValue(movieHashMatch),
                        page,
                        USER_AGENT)).execute().getData();
                // TODO is this filtering needed?
                // String name = StringUtils.lowerCase(RegExUtils.replaceAll(tvRelease.name, "[^A-Za-z]", ""));
                // String originalName = StringUtils.lowerCase(RegExUtils.replaceAll(tvRelease.originalName,
                // "[^A-Za-z]", ""));
                //     .filter(file -> {
                //     String subFileName = file.getFileName().replaceAll("[^A-Za-z]", "").toLowerCase();
                //     return subFileName.contains(name) ||
                //         (StringUtils.isNotBlank(originalName) && subFileName.contains(originalName));
                // })
            });
    }


    public @Nullable String getDownloadUrl(int fileId) throws OpenSubtitleApiException {
        return getCache("downloadUrl", b -> b.add("fileId", fileId))
            .get(() -> {
                try (HttpClient client = HttpClient.newHttpClient()) {

                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.opensubtitles.com/api/v1/download"))
                        .header("Accept", "application/json")
                        .header("Api-Key", APIKEY)
                        .header("Authorization", "Bearer " + getBearerToken(credentials.username, credentials.password))
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Test v1.0")
                        .POST(HttpRequest.BodyPublishers.ofString(
                            new ObjectMapper().writeValueAsString(Map.of("file_id", fileId))))
                        //.POST(HttpRequest.BodyPublishers.ofString("{\"file_id\":\"" + fileId + "\"}"))
                        .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() / 200 == 1) {
                        return JsonParser.parseString(response.body()).getAsJsonObject().get("link").getAsString();
                    } else {
                        throw new OpenSubtitleApiException(HttpStatus.fromStatusCode(response.statusCode()),
                            response.body(), CacheStrategy.CACHE_DISABLED, LogLevel.ERROR);
                    }
                } catch (IOException | InterruptedException | JsonSyntaxException e) {
                    throw new OpenSubtitleApiException(SERVER_ERROR, e.getMessage(), CacheStrategy.CACHE_DISABLED,
                        LogLevel.ERROR);
                }
            });


//        return getCache("downloadUrl", b -> b.add("fileId", fileId))
//            .get(() ->
//                    apiCall(() -> downloadApi.get().download(USER_AGENT, new DownloadRequest().fileId(fileId)))
//                        .addErrorHandler(createQuotaErrorHandler())
//                        .execute().getLink()
//            );
    }

    private static <T> ExecuteCall<T, OpenSubtitleApiException> apiCall(
        ThrowingSupplier<Call<T>, OpenSubtitleApiException> supplier) {
        return RetrofitService.handleExecution(supplier, OpenSubtitleApiException::new);
    }


    private @Nullable String getValue(@Nullable ParamIntf param) {
        return param == null ? null : param.value;
    }

    private ErrorHandler<OpenSubtitleApiException> createQuotaErrorHandler() {
        return new ErrorHandler<>(
            (HttpStatus code, String errorBody) -> code == NOT_ACCEPTABLE && errorBody.contains("quota"),
            null,
            (HttpStatus _, String errorBody) -> {
                String message;
                try {
                    message = new JSONObject(errorBody).getString("message");
                } catch (JSONException e) {
                    message = "Quota exceeded. Please try again later.";
                }
                return OpenSubtitleApiException.stopContactingServer(message);
            });
    }
}
