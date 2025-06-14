package org.lodder.subtools.sublibrary.data.tvdb;

import static manifold.science.util.UnitConstants.*;
import static org.lodder.subtools.sublibrary.util.http.HttpStatus.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.tvdb.api.LoginApi;
import com.tvdb.api.SearchApi;
import com.tvdb.api.SeriesApi;
import com.tvdb.invoker.ApiClient;
import com.tvdb.invoker.auth.HttpBearerAuth;
import com.tvdb.model.LoginPost200Response;
import com.tvdb.model.LoginPost200ResponseData;
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
import org.lodder.subtools.sublibrary.data.tvdb.exception.TvdbException;
import org.lodder.subtools.sublibrary.util.http.RetrofitService;
import org.lodder.subtools.sublibrary.util.http.RetrofitService.ExecuteCall;
import org.lodder.subtools.sublibrary.util.lazy.LazyThrowingSupplier;
import retrofit2.Call;

@NullMarked
public class TvdbApi implements ApiIntf {

    private static final String APIKEY = "a24ed54e-439f-4905-ae70-d22e500d19b6";
    //"cdda250d-4fb1-4134-9555-1f51ad800590";
    @val @override Manager manager;
    @val @override String provider = "TVDB";
    private final LazyThrowingSupplier<ApiClient, TvdbException> apiClient = new LazyThrowingSupplier<>(() -> {
        HttpBearerAuth bearerAuth = new HttpBearerAuth("bearer");
        bearerAuth.setBearerToken(getBearerToken());
        return new ApiClient(new OkHttpClient.Builder().addInterceptor(bearerAuth).build());
    });
    private final LazyThrowingSupplier<SearchApi, TvdbException> searchApi =
        new LazyThrowingSupplier<>(() -> apiClient.get().createService(SearchApi.class));
    private final LazyThrowingSupplier<SeriesApi, TvdbException> seriesApi =
        new LazyThrowingSupplier<>(() -> apiClient.get().createService(SeriesApi.class));

    public TvdbApi(Manager manager, String apikey) {
        this.manager = manager;
    }

    private String getBearerToken() throws TvdbException {
        try {
            return manager.getCache(CacheType.DISK, new CacheKeyBuilder("tvdb", "bearerToken"))
                .getOptional(
                    supplier:() -> Optional.ofNullable(new ApiClient().createService(LoginApi.class)
                            .loginPost(new LoginPostRequest().apikey(APIKEY)).execute().body())
                        .map(LoginPost200Response::getData).map(LoginPost200ResponseData::getToken),
                    timeToLive:29.5 day)
                .orElseThrow(() -> new TvdbException("Could not acquire a bearer token"));
        } catch (IOException e) {
            throw new TvdbException("Could not acquire a bearer token", e, false, false);
        }
    }

    public List<SearchResult> searchSeries(String serieName) throws TvdbException {
        return getCache("series", b -> b.add("serieName", serieName))
            .getCollection(() ->
                apiCall(() -> searchApi.get().getSearchResults(serieName.toLowerCase().replace(" ", "-").urlEncode(),
                    null, "series", null, null, null, null, null,
                    null, null, null, null, null))
                    .addErrorHandler(BAD_REQUEST, retry:false)
                    .addErrorHandler(UNAUTHORIZED, retry:false)
                    .execute().getData());
    }

    public Optional<SearchResult> searchSerie(int tvdbId) throws TvdbException {
        return getCache("serie", b -> b.add("tvdbId", tvdbId))
            .getOptional(() ->
                Optional.ofNullable(apiCall(() -> searchApi.get().getSearchResults(null, null, "series",
                        null, null, null, null, null, null,
                        null, String.valueOf(tvdbId), null, null))
                        .addErrorHandler(BAD_REQUEST, retry:false)
                        .addErrorHandler(UNAUTHORIZED, retry:false)
                        .execute().getData())
                    .flatMap(s -> s.stream().findFirst()));
    }

    public Optional<SeriesBaseRecord> searchEpisode(int tvdbId, int season, int episode, Language language)
        throws TvdbException {
        return getCache("episode",
            b -> b.add("tvdbId", tvdbId).add("season", season).add("episode", episode)
                .add("language", language))
            .getOptional(() ->
                Optional.ofNullable(apiCall(
                    () -> seriesApi.get().getSeriesSeasonEpisodesTranslated(BigDecimal.valueOf(tvdbId), "default",
                        language.iso639_3, 1))
                    .addErrorHandler(BAD_REQUEST, retry:false)
                    .addErrorHandler(UNAUTHORIZED, retry:false)
                    .addErrorHandler(NOT_FOUND, retry:false)
                    .execute().getData().getSeries()));
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

    private static <T> ExecuteCall<T, TvdbException> apiCall(ThrowingSupplier<Call<T>, TvdbException> supplier)
        throws TvdbException {
        return RetrofitService.handleExecution(supplier, statusCode -> new TvdbException(statusCode.description));
    }
}
