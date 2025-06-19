package org.lodder.subtools.sublibrary.data.tvdb;

import static manifold.science.util.UnitConstants.*;
import java.io.IOException;
import java.util.List;

import com.tvdb.api.LoginApi;
import com.tvdb.api.SearchApi;
import com.tvdb.api.SeriesApi;
import com.tvdb.invoker.ApiClient;
import com.tvdb.invoker.auth.HttpBearerAuth;
import com.tvdb.model.LoginPost200Response;
import com.tvdb.model.LoginPostRequest;
import com.tvdb.model.SearchResult;
import com.tvdb.model.SeriesBaseRecord;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import okhttp3.OkHttpClient;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.data.tvdb.exception.TvdbApiException;
import org.lodder.subtools.sublibrary.util.http.RetrofitService;
import org.lodder.subtools.sublibrary.util.lazy.LazyThrowingSupplier;
import retrofit2.Call;

@NullMarked
public class TvdbApi implements ApiIntf {

    private static final String APIKEY = "a24ed54e-439f-4905-ae70-d22e500d19b6";
    //"cdda250d-4fb1-4134-9555-1f51ad800590";
    @val @override Manager manager;
    @val @override String provider = "TVDB";
    private final LazyThrowingSupplier<ApiClient, TvdbApiException> apiClient = new LazyThrowingSupplier<>(() -> {
        HttpBearerAuth bearerAuth = new HttpBearerAuth("bearer");
        bearerAuth.setBearerToken(getBearerToken());
        return new ApiClient(
            new OkHttpClient.Builder()
//                .addInterceptor(new HttpLoggingInterceptor()
//                    .setLevel(HttpLoggingInterceptor.Level.BODY))
//                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
//                    .header("Accept-Encoding", "identity").build()))
                .addInterceptor(bearerAuth).build());
    });
    private final LazyThrowingSupplier<SearchApi, TvdbApiException> searchApi =
        new LazyThrowingSupplier<>(() -> apiClient.get().createService(SearchApi.class));
    private final LazyThrowingSupplier<SeriesApi, TvdbApiException> seriesApi =
        new LazyThrowingSupplier<>(() -> apiClient.get().createService(SeriesApi.class));

    public TvdbApi(Manager manager, String apikey) {
        this.manager = manager;
    }

    private String getBearerToken() throws TvdbApiException {
//        try {
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
//        } catch (IOException e) {
//            throw new TvdbApiException(NO_CONTENT, "Could not acquire a bearer token", CACHE_TEMPORARY, ERROR);
//        }
    }

    public List<SearchResult> searchSeries(String serieName) throws TvdbApiException {
        return getCache("series", b -> b.add("serieName", serieName))
            .getCollection(() ->
                apiCall(() -> searchApi.get().getSearchResults(serieName.toLowerCase().replace(" ", "-").urlEncode(),
                    null, "series", null, null, null, null, null,
                    null, null, null, null, null))
//                    .addErrorHandler(BAD_REQUEST, retry:false)
//                    .addErrorHandler(UNAUTHORIZED, retry:false, logLevel:WARN)
                    .getData());
    }

    public SearchResult searchSerie(int tvdbId) throws TvdbApiException {
        return getCache("serie", b -> b.add("tvdbId", tvdbId))
            .get(() -> {
                List<SearchResult> series = apiCall(() -> searchApi.get().getSearchResults(null, null, "series",
                    null, null, null, null, null, null,
                    null, String.valueOf(tvdbId), null, null)).getData();
//                        .addErrorHandler(ERR_BAD_REQUEST, retry:false)
//                        .addErrorHandler(ERR_UNAUTHORIZED, retry:false, logLevel:WARN)
                if (series == null || series.isEmpty()) {
                    throw TvdbApiException.noResult("Serie with tvdb id [$tvdbId] not found");
                }
                return series.getFirst();
            });
    }

    public SeriesBaseRecord searchEpisode(int tvdbId, int season, int episode, Language language)
        throws TvdbApiException {
        return getCache("episode",
            b -> b.add("tvdbId", tvdbId).add("season", season).add("episode", episode)
                .add("language", language))
            .get(() -> apiCall(
                () -> seriesApi.get()
                    .getSeriesSeasonEpisodesTranslated(tvdbId, "default", language.iso639_3, 0)).getData());
//                    .addErrorHandler(BAD_REQUEST, retry:false)
//                    .addErrorHandler(UNAUTHORIZED, retry:false, logLevel:WARN)
//                    .addErrorHandler(NOT_FOUND, retry:false, logLevel:INFO)
//                if(response.)
//                if (data == null || data.getSeries() == null) {
//                    throw TvdbApiException.noResult("Could not find episode for tvdbId[$tvdbId], season[$season]," +
//                        " episode [$episode], language [$language]");
//                }
//                return data.getSeries();
//            });
    }

//    private TvdbSerie seriesToTVDBSerie(Series serie, Language lang) {
//        TvdbSerie theTVDBSerie = new TvdbSerie();
//
//        theTVDBSerie.id = serie.id;
//        theTVDBSerie.airsDayOfWeek = serie.airsDayOfWeek;
//        theTVDBSerie.airsTime = serie.airsTime;
//        theTVDBSerie.contentRating = serie.rating;
//        theTVDBSerie.firstAired = serie.firstAired;
//        theTVDBSerie.genres = serie.genre;
//        theTVDBSerie.imdbId = serie.imdbId;
//        theTVDBSerie.language = lang;
//        theTVDBSerie.network = serie.network;
//        // theTVDBSerie.overview = serie.overview;
//        theTVDBSerie.rating = serie.rating;
//        theTVDBSerie.runtime = serie.runtime;
//        // theTVDBSerie.serieId = toString(serie.id);
//        theTVDBSerie.serieName = serie.seriesName;
//        theTVDBSerie.status = serie.status;
//
//        return theTVDBSerie;
//    }

//    private TvdbEpisode episodeToTVDBEpisode(Episode episode, Language lang) {
//        TvdbEpisode tvdbEpisode = new TvdbEpisode();
//
//        tvdbEpisode.id = toString(episode.id);
//        tvdbEpisode.dvdEpisodeNumber = toString(episode.dvdEpisodeNumber);
//        tvdbEpisode.dvdSeason = toString(episode.dvdSeason);
//        tvdbEpisode.episodeName = episode.episodeName;
//        tvdbEpisode.episodeNumber = episode.airedEpisodeNumber;
//        tvdbEpisode.firstAired = episode.firstAired;
//        tvdbEpisode.language = lang;
//        // tvdbEpisode.setOverview(episode.language.overview);
//        tvdbEpisode.seasonNumber = episode.airedSeason;
//        tvdbEpisode.absoluteNumber = toString(episode.absoluteNumber);
//        tvdbEpisode.lastUpdated = toString(episode.lastUpdated);
//        tvdbEpisode.seasonId = toString(episode.airedSeasonID);
//        tvdbEpisode.airsAfterSeason = 0;
//        tvdbEpisode.airsBeforeEpisode = 0;
//
//        return tvdbEpisode;
//    }

//    private String toString(Object value) {
//        return value != null ? value.toString() : null;
//    }

//    private static <T> ExecuteCall<T, TvdbApiException> apiCall(ThrowingSupplier<Call<T>, TvdbApiException> supplier)
//        throws TvdbApiException {
//        return RetrofitService.handleExecution(supplier, TvdbApiException::new);
//    }

    private static <T> T apiCall(ThrowingSupplier<Call<T>, TvdbApiException> supplier)
        throws TvdbApiException {
        return RetrofitService.handleExecution(supplier, TvdbApiException::new).execute();
    }
}
