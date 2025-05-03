package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles;

import static manifold.science.measures.TimeUnit.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.pivovarit.function.ThrowingSupplier;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
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
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.Retry;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.util.http.HttpClientException;
import org.opensubtitles.api.AuthenticationApi;
import org.opensubtitles.api.DownloadApi;
import org.opensubtitles.api.SubtitlesApi;
import org.opensubtitles.invoker.ApiClient;
import org.opensubtitles.invoker.ApiException;
import org.opensubtitles.model.DownloadRequest;
import org.opensubtitles.model.Login200Response;
import org.opensubtitles.model.LoginRequest;

public class OpenSubtitlesApi implements SubtitleApi {

    private static final String APIKEY = "3IlyaP0KNv6QmJ1gOBX8IXwzD1P9b8c0";//"lNNp0yv0ah8gytkmYPbHwuaATJqr4rS9";
    private static final ApiClient API_CLIENT;
    private static final String USER_AGENT = "SubTools";
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
        }
    }

    public void login(Credentials credentials) throws OpenSubtitleException {
        try {
            Login200Response loginResponse =
                new AuthenticationApi(API_CLIENT).login("application/json", USER_AGENT,
                    new LoginRequest().username(credentials.username).password(credentials.password));
            API_CLIENT.setBearerToken(loginResponse.getToken());
        } catch (ApiException e) {
            throw new OpenSubtitleException(e);
        }
    }

    public static boolean isValidCredentials(String userName, String password) {
        try {
            new AuthenticationApi(API_CLIENT).login("application/json", USER_AGENT,
                new LoginRequest().username(userName).password(password));
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    public List<org.opensubtitles.model.Subtitle> searchSubtitles(
        @Nullable AiTranslatedEnum aiTranslated=null,
        @Nullable Integer episode=null,
        @Nullable ForeignPartsOnlyEnum foreignPartsOnly=null,
        @Nullable HearingImpairedEnum hearingImpaired=null,
        @Nullable Integer id=null,
        @Nullable Integer imdbId=null,
        @Nullable Language language=null,
        @Nullable MachineTranslatedEnum machineTranslated=null,
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

        String userAgent = "SubTools";
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
                try {
                    return execute(
                        () -> new SubtitlesApi(API_CLIENT).subtitles(id, imdbId, tmdbId, getValue(type), query,
                            language != null ? language.langCode : null, movieHash, userId,
                            getValue(hearingImpaired), getValue(foreignPartsOnly), getValue(trustedSources),
                            getValue(machineTranslated), getValue(aiTranslated),
                            orderBy == null ? null : orderBy.paramName, getValue(orderDirection),
                            parentFeatureId, parentImdbId, parentTmdbId, season, episode, year,
                            getValue(movieHashMatch), page, userAgent)).data;
                } catch (Exception e) {
                    throw new OpenSubtitleException(e);
                }
            });
    }

    public String getDownloadUrl(int fileId) throws OpenSubtitleException {
        return getCache("downloadUrl", b -> b.add("fileId", fileId))
            .get(() -> {
                try {
                    return execute(() -> new DownloadApi(API_CLIENT)
                        .download("SubTools", new DownloadRequest().fileId(fileId))).link;
                } catch (Exception e) {
                    throw new OpenSubtitleException(e);
                }
            });
    }

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
                                5 Second)
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


    private <T> T execute(ThrowingSupplier<T, ApiException> callable) throws ApiException {
        try {
            return callable.get();
        } catch (ApiException e) {
            if (e.getCode() == 429 || e.getMessage().contains("ratelimit")) {
                // Too Many Requests
                sleep(1 Second);
                // retry
                return callable.get();
            } else {
                throw e;
            }
        }
    }

    private String getValue(ParamIntf param) {
        return param == null ? null : param.value;
    }
}
