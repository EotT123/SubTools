package org.lodder.subtools.sublibrary.data.tvdb;

import static org.lodder.subtools.multisubdownloader.Messages.*;
import static org.lodder.subtools.sublibrary.Manager.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import com.tvdb.model.SearchResult;
import com.tvdb.model.SeriesBaseRecord;
import com.uwetrottmann.thetvdb.entities.Series;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.AdapterIntf;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.data.tvdb.exception.TvdbException;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TvdbAdapter implements AdapterIntf {

    private static final Logger LOGGER = LoggerFactory.getLogger(TvdbAdapter.class);
    private static final String API_KEY = "A1720D2DDFDCE82D";
    private static TvdbAdapter instance;
    @val @override Manager manager;
    @val @override String provider = "TVDB";
    private final UserInteractionHandler userInteractionHandler;
    private final TvdbApi api;

    public TvdbAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        this.manager = manager;
        this.userInteractionHandler = userInteractionHandler;
        this.api = new TvdbApi(manager, API_KEY);
    }

    public Optional<Series> searchMovie(String title) {
        //TODO implement this
        return Optional.empty();
    }

    public Optional<SearchResult> searchSerie(String serieName) {
        String encodedSerieName = URLEncoder.encode(serieName.toLowerCase().replace(" ", "-"), StandardCharsets.UTF_8);

        CacheKey cache = getCache("series", b -> b.add("name", encodedSerieName));

        if (cache.isPresent() && (!cache.isTemporaryObject() || !cache.isExpiredTemporary())) {
            return cache.getOptional();
        }

        Optional<SearchResult> tvdbSerie;
        List<SearchResult> serieIds;
        try {
            serieIds = api.searchSeries(encodedSerieName);
        } catch (TvdbException e) {
            serieIds = List.of();
        }
        if (serieIds.isEmpty()) {
            tvdbSerie = Optional.empty();
        } else if (!userInteractionHandler.settings.optionsConfirmProviderMapping && serieIds.size() == 1) {
            tvdbSerie = Optional.of(serieIds.first);
        } else {
            Comparator<SearchResult> comparator =
                Comparator.comparing((SearchResult s) -> ProviderId.calculateLevenshteinDistance(serieName, s.name));
            try {
                tvdbSerie = userInteractionHandler.selectFromList(
                    serieIds.stream().sorted(comparator).toList(),
                    getText("Prompter.SelectTvdbMatchForSerie", serieName),
                    provider,
                    s -> "${s.name} (${s.firstAirTime})");
                if (tvdbSerie.isEmpty()) {
                    LOGGER.error("Unknown serie name in tvdb: $serieName");
                    tvdbSerie = promptUserToEnterTvdbId(serieName)
                        .mapToObjEx(tvdbId -> api.searchSerie(tvdbId).orElse(null));
                }
            } catch (TvdbException e) {
                tvdbSerie = Optional.empty();
            }
        }
        if (tvdbSerie.isEmpty()) {
            cache.storeTempValue(Value.ofOptional(tvdbSerie));
        } else {
            cache.store(Value.ofOptional(tvdbSerie));
            Optional<SerieMapping> serieMapping =
                tvdbSerie.map((SearchResult serie) -> new SerieMapping(serieName, serie.tvdbId, serie.name));
            getCache("serieId", b -> b.add("name", encodedSerieName))
                .store(
                    value:Value.ofOptional(serieMapping),
                    storeTempNullValue:true);
        }
        return tvdbSerie;
    }

    public Optional<SeriesBaseRecord> searchEpisode(int tvdbId, int season, int episode) {
        return getCache("episode", b -> b.add("tvdbId", tvdbId).add("season", season).add("episode", episode))
            .getOptional(
                () -> {
                    try {
                        return api.searchEpisode(tvdbId, season, episode, Language.ENGLISH);
                    } catch (TvdbException e) {
                        LOGGER.error(
                            "API $provider getEpisode for serie id [$tvdbId] %s (${e.getMessage()})".formatted(
                                TvRelease.formatSeasonEpisode(season, episode)), e);
                        return Optional.empty();
                    }
                },
                storeTempNullValue:true);
    }

    public static synchronized TvdbAdapter getInstance(Manager manager,
        UserInteractionHandler userInteractionHandler) {
        if (instance == null) {
            instance = new TvdbAdapter(manager, userInteractionHandler);
        }
        return instance;
    }

    private OptionalInt promptUserToEnterTvdbId(String showName) {
        return userInteractionHandler.enter(getText("InputPanel.EnterTvdbId", showName))
            .map(tvdbidString -> {
                try {
                    return OptionalInt.of(Integer.parseInt(tvdbidString));
                } catch (NumberFormatException e) {
                    LOGGER.error(getText("InputPanel.invalid.tvdbid", tvdbidString));
                    return promptUserToEnterTvdbId(showName);
                }
            }).orElseGet(OptionalInt::empty);
    }
}
