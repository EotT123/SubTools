package org.lodder.subtools.sublibrary.data.tvdb;

import static util.Utils.*;

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
import retrofit.ErrorResponse;
import retrofit.SuccessfulResponse;

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
                return switch (theTvdb.search().series(encodedSerieName, null, null, null, null).call()) {
                    case SuccessfulResponse<SeriesResultsResponse> r -> r.body.data.stream()
                        .map(series -> new SearchResult().tvdbId(String.valueOf(series.id)).name(series.seriesName))
                        .toList();
                    case ErrorResponse r -> throw handleErrorResponse(r, "Could not find serie with name [$serieName]");
                };
            });
    }

    public SeriesBaseRecord searchSerieWithTvdbId(int tvdbId) throws TvdbApiException {
        return getCache("serie", b -> b.add("tvdbId", tvdbId))
            .get(
                () -> switch (theTvdb.series().series(tvdbId, null).call()) {
                    case SuccessfulResponse<SeriesResponse> response -> {
                        Series series = response.body.data;
                        yield new SeriesBaseRecord().id(series.id).name(series.seriesName);
                    }
                    case ErrorResponse r -> throw handleErrorResponse(r, "Could not find show with id [$tvdbId]");
                });
    }

    //  Allows searching for an IMDB or EIDR id
    public SeriesBaseRecord searchSerieWithRemoteId(String remoteId) throws TvdbApiException {
        return getCache("serie", b -> b.add("remoteId", remoteId))
            .get(
                () -> switch (theTvdb.search().series(null, remoteId, null, null, null).call()) {
                    case SuccessfulResponse<SeriesResultsResponse> response -> {
                        Series series = response.body.data.first;
                        yield new SeriesBaseRecord().id(series.id).name(series.seriesName);
                    }
                    case ErrorResponse r ->
                        throw handleErrorResponse(r, "Could not find show with remote id [$remoteId]");
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
            .get(() -> switch (theTvdb.series()
                .episodesQuery(tvdbId, null, season, episode, null, null, null, null, null, language.iso639_1).call()) {
                case SuccessfulResponse<EpisodesResponse> response -> {
                    List<Episode> episodes = response.body.data;
                    if (episodes == null || episodes.isEmpty()) {
                        yield null;
                    }
                    Episode ep = episodes.first;
                    yield new TvdbEpisode(ep.id, ifNotNull(ep.seriesId, Integer::longValue), ep.episodeName,
                        ep.airedEpisodeNumber, ep.airedSeason, null);
                }
                case ErrorResponse r -> throw handleErrorResponse(r, "Could not find episode with" +
                    "tvdbId[$tvdbId], season[$season], episode[$episode], language[$language]");
            });
    }

    private TvdbApiException handleErrorResponse(ErrorResponse errorResponse, String message) {
        return new TvdbApiException(errorResponse.code, message + " - " + errorResponse.message,
            errorResponse.cacheStrategy, errorResponse.logLevel);
    }
}
