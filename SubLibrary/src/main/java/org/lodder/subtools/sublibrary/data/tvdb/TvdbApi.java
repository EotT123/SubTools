package org.lodder.subtools.sublibrary.data.tvdb;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.tvdb.api.SearchApi;
import com.tvdb.api.SeriesApi;
import com.tvdb.invoker.ApiClient;
import com.tvdb.model.GetSearchResults200Response;
import com.tvdb.model.GetSeriesSeasonEpisodesTranslated200Response;
import com.tvdb.model.GetSeriesSeasonEpisodesTranslated200ResponseData;
import com.tvdb.model.SearchResult;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.data.tvdb.exception.TvdbException;
import org.lodder.subtools.sublibrary.util.http.RetrofitService;
import org.lodder.subtools.sublibrary.util.http.RetrofitService.ExecuteCall;
import retrofit2.Call;
import retrofit2.Response;

public class TvdbApi implements ApiIntf {

    private static final String APIKEY = "cdda250d-4fb1-4134-9555-1f51ad800590";
    //    private static final ApiClient API_CLIENT;
    @val @override Manager manager;
    @val @override String provider = "TVDB";
    //    private final TheTvdb api;
    private static final SearchApi SEARCH_API;
    private static final SeriesApi SERIES_API;

    static {
        ApiClient apiClient = new ApiClient();
        apiClient.setApiKey(APIKEY);
        SEARCH_API = apiClient.createService(SearchApi.class);
        SERIES_API = apiClient.createService(SeriesApi.class);
    }

    public TvdbApi(Manager manager, String apikey) {
        this.manager = manager;
    }

    public List<SearchResult> searchSeries(String serieName) throws TvdbException {
        return getCache("series", b -> b.add("serieName", serieName))
            .getCollection(() -> {
                String encodedSerieName = serieName.toLowerCase().replace(" ", "-").urlEncode();
                try {
                    Response<GetSearchResults200Response> response =
                        SEARCH_API.getSearchResults(encodedSerieName, null, "series", null, null,
                                null, null, null, null, null, null, null, null)
                            .execute();
                    return response.isSuccessful() ? response.body().getData() : List.of();
                } catch (IOException e) {
                    throw new TvdbException(e);
                }
            });
    }

    public Optional<SearchResult> searchSerie(int tvdbId) throws TvdbException {
        return getCache("serie", b -> b.add("tvdbId", tvdbId))
            .getOptional(() -> {
                try {
                    Response<GetSearchResults200Response> response =
                        SEARCH_API.getSearchResults(null, null, "series", null, null,
                                null, null, null, null, null, String.valueOf(tvdbId), null, null)
                            .execute();
                    return response.isSuccessful() ? Optional.ofNullable(response.body().getData().firstOrNull()) :
                        Optional.empty();
                } catch (IOException e) {
                    throw new TvdbException(e);
                }
            });
    }

    public Optional<GetSeriesSeasonEpisodesTranslated200ResponseData> searchEpisode(int tvdbId, int season, int episode,
        Language language) throws TvdbException {
        return getCache("episode",
            b -> b.add("tvdbId", tvdbId).add("season", season).add("episode", episode)
                .add("language", language))
            .getOptional(() -> {
                try {
                    Response<GetSeriesSeasonEpisodesTranslated200Response> response =
                        SERIES_API.getSeriesSeasonEpisodesTranslated(BigDecimal.valueOf(tvdbId), "default",
                            language.iso639_3, 1).execute();
                    return response.isSuccessful() ? Optional.ofNullable(response.body().getData()) : Optional.empty();
                } catch (IOException e) {
                    throw new TvdbException(e);
                }
            });
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

    private static <T> ExecuteCall<T, TvdbException> apiCall(ThrowingSupplier<Call<T>,
        IOException> supplier) {
        return RetrofitService.handleExecution(supplier, TvdbException::new);
    }
}
