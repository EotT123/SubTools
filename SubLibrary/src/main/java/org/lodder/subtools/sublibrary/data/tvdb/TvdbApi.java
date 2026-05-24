package org.lodder.subtools.sublibrary.data.tvdb;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.tvdb.model.SearchResult;
import com.tvdb.model.SeriesBaseRecord;
import com.uwetrottmann.thetvdb.TheTvdb;
import com.uwetrottmann.thetvdb.entities.Episode;
import com.uwetrottmann.thetvdb.entities.EpisodesResponse;
import com.uwetrottmann.thetvdb.entities.Series;
import com.uwetrottmann.thetvdb.entities.SeriesResponse;
import com.uwetrottmann.thetvdb.entities.SeriesResultsResponse;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.data.tvdb.exception.TvdbApiException;
import org.lodder.subtools.sublibrary.data.tvdb.model.TvdbEpisode;
import retrofit2.Response;

@NullMarked
public class TvdbApi implements ApiIntf {

    private final TheTvdb theTvdb;

    @val @override String provider = "TVDB";

    public TvdbApi(String apikey) {
        this.theTvdb = new TheTvdb(apikey);
    }

    public List<SearchResult> searchSeries(String serieName) throws TvdbApiException {
        return getCache("series", b -> b.add("serieName", serieName))
            .get(() -> {
                String encodedSerieName =
                    URLEncoder.encode(serieName.toLowerCase().replace(" ", "-"), StandardCharsets.UTF_8);
                try {
                    Response<SeriesResultsResponse> response =
                        theTvdb.search()
                            .series(encodedSerieName, null, null, null, null)
                            .execute();
                    if (response.isSuccessful()) {
                        return response.body().data.stream()
                            .map(series -> new SearchResult().tvdbId(String.valueOf(series.id)).name(series.seriesName))
                            .toList();
                    }
                    return List.of();
                } catch (IOException e) {
                    throw TvdbApiException.error(e);
                }
            });
    }

    public SeriesBaseRecord searchSerieWithTvdbId(int tvdbId) throws TvdbApiException {
        return getCache("serie", b -> b.add("tvdbId", tvdbId))
            .get(() -> {
                try {
                    Response<SeriesResponse> response = theTvdb.series().series(tvdbId, null).execute();
                    if (response.isSuccessful()) {
                        Series series = response.body().data;
                        return new SeriesBaseRecord().id(series.id).name(series.seriesName);
                    }
                    throw TvdbApiException.noResult("Could not find tvdb show with id " + tvdbId);
                } catch (IOException e) {
                    throw TvdbApiException.error(e);
                }
            });
    }

    //  Allows searching for an IMDB or EIDR id
    public SeriesBaseRecord searchSerieWithRemoteId(String remoteId) throws TvdbApiException {
        return getCache("serie", b -> b.add("remoteId", remoteId))
            .get(() -> {
                try {
                    Response<SeriesResultsResponse> response =
                        theTvdb.search().series(null, remoteId, null, null, null).execute();
                    if (response.isSuccessful()) {
                        Series series = response.body().data.getFirst();
                        return new SeriesBaseRecord().id(series.id).name(series.seriesName);
                    }
                    throw TvdbApiException.noResult("Could not find tvdb show with remote id " + remoteId);
                } catch (IOException e) {
                    throw TvdbApiException.error(e);
                }
            });
    }

    public @Nullable TvdbEpisode searchEpisode(int tvdbId, int season, int episode, Language language)
        throws TvdbApiException {

        return getCache("episode",
            b -> b
                .add("tvdbId", tvdbId)
                .add("season", season)
                .add("episode", episode)
                .add("language", language))
            .get(() -> {
                try {
                    Response<EpisodesResponse> response = theTvdb.series().episodesQuery(tvdbId, null, season, episode,
                        null, null, null, null, null, language.iso639_1).execute();
                    if (response.isSuccessful()) {
                        Episode ep = response.body().data.getFirst();
                        return new TvdbEpisode(ep.id, ep.seriesId.longValue(), ep.episodeName, ep.airedEpisodeNumber,
                            ep.airedSeason, null);
                    }
                    return null;
                } catch (IOException e) {
                    throw TvdbApiException.error(e);
                }
            });
    }
}
