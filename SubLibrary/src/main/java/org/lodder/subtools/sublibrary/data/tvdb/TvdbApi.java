package org.lodder.subtools.sublibrary.data.tvdb;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.uwetrottmann.thetvdb.TheTvdb;
import com.uwetrottmann.thetvdb.entities.Episode;
import com.uwetrottmann.thetvdb.entities.EpisodesResponse;
import com.uwetrottmann.thetvdb.entities.Series;
import com.uwetrottmann.thetvdb.entities.SeriesResponse;
import com.uwetrottmann.thetvdb.entities.SeriesResultsResponse;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.data.tvdb.exception.TvdbException;
import retrofit2.Response;

public class TvdbApi implements ApiIntf {

    private final TheTvdb api;
    @val @override Manager manager;
    @val @override String provider = "TVDB";

    public TvdbApi(Manager manager, String apikey) {
        this.manager =manager;
        this.api = new TheTvdb(apikey);
    }

    public List<Series> searchSeries(String serieName, @Nullable Language language=null) throws TvdbException {
        return getCache("series", b -> b.add("serieName", serieName).add("language", language))
            .getCollection(() -> {
                String encodedSerieName = serieName.toLowerCase().replace(" ", "-").urlEncode();
                try {
                    Response<SeriesResultsResponse> response =
                        api.search()
                            .series(encodedSerieName, null, null, null,
                                language == null ? null : language.langCode)
                            .execute();
                    if (response.isSuccessful() && response.body() != null) {
                        return response.body().data;
//                        return response.body().data
//                        .stream()
//                            .map(series -> seriesToTVDBSerie(series, language))
//                            .toList();
                    }
                    return List.of();
                } catch (IOException e) {
                    throw new TvdbException(e);
                }
            });
    }

    public Optional<Series> searchSerie(int tvdbId, @Nullable Language language=null) throws TvdbException {
        return getCache("serie", b -> b.add("tvdbId", tvdbId).add("language", language))
            .getOptional(() -> {
                try {
                    Response<SeriesResponse> response =
                        api.series()
                            .series(tvdbId, language == null ? null : language.langCode)
                            .execute();
                    if (response.isSuccessful() && response.body() != null) {
                        return Optional.ofNullable(response.body().data);
//                return Optional.of(seriesToTVDBSerie(response.body().data, language));
                    }
                    return Optional.empty();
                } catch (IOException e) {
                    throw new TvdbException(e);
                }
            });
    }

    public Optional<Episode> searchEpisode(int tvdbId, int season, int episode, @Nullable Language language=null)
        throws TvdbException {
        return getCache("episode",
            b -> b.add("tvdbId", tvdbId).add("season", season).add("episode", episode)
                .add("language", language))
            .getOptional(() -> {
                try {
                    Response<EpisodesResponse> response =
                        api.series()
                            .episodesQuery(tvdbId, null, season, episode, null, null, null, null, null,
                                language == null ? null : language.langCode)
                            .execute();
                    if (response.isSuccessful() && response.body() != null && response.body().data != null &&
                        !response.body().data.isEmpty()) {
                        return Optional.ofNullable(response.body().data.first());
//                        if (response.body().data == null) {
//                            return Optional.empty();
//                        }
//                        return response.body().data.stream()
//                            .map(serie -> episodeToTVDBEpisode(serie, language))
//                            .findFirst();
                    }
                    throw new TvdbException(response.errorBody().string());
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
}
