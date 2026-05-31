package org.lodder.subtools.sublibrary.data.tvdb;

import static manifold.science.util.UnitConstants.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static org.lodder.subtools.sublibrary.LogLevel.*;
import static util.Utils.*;

import java.util.List;

import com.tvdb.api.LoginApi;
import com.tvdb.api.SearchApi;
import com.tvdb.api.SeriesApi;
import com.tvdb.invoker.ApiClient;
import com.tvdb.invoker.auth.HttpBearerAuth;
import com.tvdb.model.GetSearchResultsByRemoteId200Response;
import com.tvdb.model.GetSeriesBase200Response;
import com.tvdb.model.LoginPost200Response;
import com.tvdb.model.LoginPost200ResponseData;
import com.tvdb.model.LoginPostRequest;
import com.tvdb.model.SearchByRemoteIdResult;
import com.tvdb.model.SearchResult;
import com.tvdb.model.SeriesBaseRecord;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import okhttp3.OkHttpClient;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.ManagerException;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.connection.retrofit.ErrorResponse;
import org.lodder.subtools.sublibrary.connection.retrofit.SuccessfulResponse;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.data.tvdb.exception.TvdbApiException;
import org.lodder.subtools.sublibrary.data.tvdb.model.TvdbEpisode;
import org.lodder.subtools.sublibrary.util.lazy.LazyThrowingSupplier;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;

@NullMarked
public class TvdbApiBak implements ApiIntf {

    private static final String APIKEY = "d2dd1b0f-0eb3-45c9-9c62-f852ad857aa3";
    //"cdda250d-4fb1-4134-9555-1f51ad800590";
    @val @override String provider = "TVDB";
    private final LazyThrowingSupplier<ApiClient, TvdbApiException> apiClient = new LazyThrowingSupplier<>(() -> {
        HttpBearerAuth bearerAuth = new HttpBearerAuth("bearer");
        bearerAuth.setBearerToken(getBearerToken());
        return new ApiClient(new OkHttpClient.Builder().addInterceptor(bearerAuth).build());
    });
    private final LazyThrowingSupplier<SearchApi, TvdbApiException> searchApi =
        new LazyThrowingSupplier<>(() -> apiClient.get().createService(SearchApi.class));
    private final LazyThrowingSupplier<SeriesApi, TvdbApiException> seriesApi =
        new LazyThrowingSupplier<>(() -> apiClient.get().createService(SeriesApi.class));

    public TvdbApiBak(String apikey) {
    }

    private String getBearerToken() throws TvdbApiException {
        return Manager.getCache(CacheType.DISK, new CacheKeyBuilder("tvdb", "bearerToken"))
            .get(
                supplier:() -> switch (new ApiClient().createService(LoginApi.class)
                    .loginPost(new LoginPostRequest().apikey(APIKEY)).call()) {
                    case SuccessfulResponse<LoginPost200Response> r -> {
                        String token = ifNotNull(r.body.data, LoginPost200ResponseData::getToken);
                        if (token == null) {
                            HttpStatus code = ifNullThen(HttpStatus.fromStatusCode(r.code), HttpStatus.NO_CONTENT);
                            throw new TvdbApiException(code, "Could not acquire a bearer token - " + r.message,
                                CACHE_TEMPORARY, ERROR);
                        }
                        yield token;
                    }
                    case ErrorResponse r -> throw handleErrorResponse(r, "Could not acquire a bearer token");
                },
                timeToLive:29.5 day);
    }

    public List<SearchResult> searchSeries(String serieName) throws TvdbApiException {
//        return getCache("series", b -> b.add("serieName", serieName))
//            .getCollection(() ->
//                apiCall(() -> searchApi.get().getSearchResults(serieName.toLowerCase().replace(" ", "-").urlEncode(),
//                    null, "series", null, null, null, null, null,
//                    null, null, null, null, null))
//                    .getData());

        try {
            return getCache("series", b -> b.add("serieName", serieName))
                .get(() ->
                    Manager.getDocument(new PageContentParams(
                            "https://www.thetvdb.com/search?query=" +
                                serieName.toLowerCase().replace(" ", "%20").urlEncode()))
                        .select(".ais-Hits-item").stream().map(elem ->
                            new SearchResult()
                                .tvdbId(substringAfterLast(elem.selectFirst(".media-body .text-muted").text(), "#"))
                                .name(elem.selectFirst(".media-heading a").text())
                        ).toList());
        } catch (ManagerException e) {
            throw TvdbApiException.error(e);
        }
    }

    public SeriesBaseRecord searchSerieWithTvdbId(int tvdbId) throws TvdbApiException {
//        return getCache("serie", b -> b.add("tvdbId", tvdbId))
//            .get(() -> apiCall(() -> seriesApi.get().getSeriesBase(tvdbId)).getData());
        try {
            return getCache("serie", b -> b.add("tvdbId", tvdbId))
                .get(() -> new SeriesBaseRecord()
                    .id(tvdbId)
                    .name(Manager.getDocument(new PageContentParams("https://www.thetvdb.com/?tab=series&id=" + tvdbId))
                        .selectFirst("#series_title").text()));
        } catch (ManagerException e) {
            throw TvdbApiException.error(e);
        }
    }

    //  Allows searching for an IMDB or EIDR id
    public SeriesBaseRecord searchSerieWithRemoteId(String remoteId) throws TvdbApiException {
        return getCache("serie", b -> b.add("remoteId", remoteId))
            .get(() ->
                switch (searchApi.get().getSearchResultsByRemoteId(remoteId).call()) {
                    case SuccessfulResponse<GetSearchResultsByRemoteId200Response> r -> {
                        List<SearchByRemoteIdResult> data = r.body.data;
                        if (data == null || data.isEmpty() || data.first.series == null) {
                            throw TvdbApiException.noResult("Serie with remote id [$remoteId] not found");
                        }
                        yield data.first.series;
                    }
                    case ErrorResponse r ->
                        throw handleErrorResponse(r, "Could not find show with remote id [$remoteId]");
                }
            );
    }

    public @Nullable TvdbEpisode searchEpisode(int tvdbId, int season, int episode, Language language)
        throws TvdbApiException {
        return getCache("episode",
            b -> b.add("tvdbId", tvdbId).add("season", season).add("episode", episode)
                .add("language", language))
            .get(() ->
                switch (seriesApi.get().getSeriesSeasonEpisodesTranslated(tvdbId, "default", language.iso639_3, 0)
                    .call()) {
                    case SuccessfulResponse<GetSeriesBase200Response> r ->
                        ifNotNullOrElseThrow(r.body.data, TvdbEpisode::new,
                            () -> TvdbApiException.noResult("Could not find serie with tvdbId[$tvdbId]," +
                                " season[$season], episode[$episode], language[$language]"));
                    case ErrorResponse r -> throw handleErrorResponse(r, "Could not find serie with tvdbId[$tvdbId]," +
                        " season[$season], episode[$episode], language[$language]");
                }
            );
    }

    private TvdbApiException handleErrorResponse(ErrorResponse errorResponse, String message) {
        return new TvdbApiException(errorResponse.code, message + " - " + errorResponse.message,
            errorResponse.cacheStrategy, errorResponse.logLevel);
    }
}
