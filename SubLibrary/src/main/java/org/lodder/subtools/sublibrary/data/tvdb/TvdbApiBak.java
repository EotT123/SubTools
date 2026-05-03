package org.lodder.subtools.sublibrary.data.tvdb;

import static manifold.science.util.UnitConstants.*;
import static org.apache.commons.lang3.StringUtils.*;
import static util.Utils.*;

import java.io.IOException;
import java.util.List;

import com.tvdb.api.LoginApi;
import com.tvdb.api.SearchApi;
import com.tvdb.api.SeriesApi;
import com.tvdb.invoker.ApiClient;
import com.tvdb.invoker.auth.HttpBearerAuth;
import com.tvdb.model.LoginPost200Response;
import com.tvdb.model.LoginPostRequest;
import com.tvdb.model.SearchByRemoteIdResult;
import com.tvdb.model.SearchResult;
import com.tvdb.model.SeriesBaseRecord;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import okhttp3.OkHttpClient;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.ManagerException;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.data.tvdb.exception.TvdbApiException;
import org.lodder.subtools.sublibrary.data.tvdb.model.TvdbEpisode;
import org.lodder.subtools.sublibrary.util.lazy.LazyThrowingSupplier;
import org.lodder.subtools.sublibrary.util.webpage.http.RetrofitService;
import retrofit2.Call;

@NullMarked
public class TvdbApiBak implements ApiIntf {

    private static final String APIKEY = "d2dd1b0f-0eb3-45c9-9c62-f852ad857aa3";
    //"cdda250d-4fb1-4134-9555-1f51ad800590";
    @val @override Manager manager;
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

    public TvdbApiBak(Manager manager, String apikey) {
        this.manager = manager;
    }

    private String getBearerToken() throws TvdbApiException {
        return manager.getCache(CacheType.DISK, new CacheKeyBuilder("tvdb", "bearerToken"))
            .get(
                supplier:() -> {
                    try {
                        LoginPost200Response response = new ApiClient().createService(LoginApi.class)
                            .loginPost(new LoginPostRequest().apikey(APIKEY)).execute().body();
                        if (response == null || response.getData() == null) {
                            throw TvdbApiException.noResult("Could not acquire a bearer token");
                        }
                        return response.getData().getToken();
                    } catch (IOException e) {
                        throw TvdbApiException.error(e);
                    }
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
                    manager.get(new PageContentParams(
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
                    .name(manager.get(
                            new PageContentParams("https://www.thetvdb.com/?tab=series&id=" + tvdbId)).
                        selectFirst("#series_title").text()));
        } catch (ManagerException e) {
            throw TvdbApiException.error(e);
        }
    }

    //  Allows searching for an IMDB or EIDR id
    public SeriesBaseRecord searchSerieWithRemoteId(String remoteId) throws TvdbApiException {
        return getCache("serie", b -> b.add("remoteId", remoteId))
            .get(() -> {
                List<SearchByRemoteIdResult> data =
                    apiCall(() -> searchApi.get().getSearchResultsByRemoteId(remoteId)).getData();
                if (data == null || data.isEmpty()) {
                    throw TvdbApiException.noResult("Serie with remote id [$remoteId] not found");
                }
                return data.getFirst().getSeries();
            });
    }

    public @Nullable TvdbEpisode searchEpisode(int tvdbId, int season, int episode, Language language)
        throws TvdbApiException {
        return getCache("episode",
            b -> b.add("tvdbId", tvdbId).add("season", season).add("episode", episode)
                .add("language", language))
            .get(() -> ifNotNull(apiCall(
                    () -> seriesApi.get()
                        .getSeriesSeasonEpisodesTranslated(tvdbId, "default", language.iso639_3, 0)).getData(),
                TvdbEpisode::new));
    }

    private static <T> T apiCall(ThrowingSupplier<Call<T>, TvdbApiException> supplier)
        throws TvdbApiException {
        return RetrofitService.handleExecution(supplier, TvdbApiException::new).execute();
    }
}
