package org.lodder.subtools.sublibrary.data.tvdb;

import static java.util.Objects.*;
import static org.lodder.subtools.multisubdownloader.Messages.*;
import static org.lodder.subtools.sublibrary.Manager.*;
import static org.lodder.subtools.sublibrary.model.ProviderIdType.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tvdb.model.MovieBaseRecord;
import com.tvdb.model.SearchResult;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.AdapterIntf;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.data.tvdb.exception.TvdbApiException;
import org.lodder.subtools.sublibrary.data.tvdb.exception.TvdbException;
import org.lodder.subtools.sublibrary.data.tvdb.model.TvdbEpisode;
import org.lodder.subtools.sublibrary.data.tvdb.model.TvdbSerie;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.lodder.subtools.sublibrary.util.lazy.LazyBiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class TvdbAdapter implements AdapterIntf {

    private static final Logger LOGGER = LoggerFactory.getLogger(TvdbAdapter.class);
    private static final String API_KEY = "A1720D2DDFDCE82D";
    private static final LazyBiFunction<Manager, UserInteractionHandler, TvdbAdapter> INSTANCE =
        new LazyBiFunction<>(TvdbAdapter::new);
    @val @override Manager manager;
    @val @override String provider = "TVDB";
    private final UserInteractionHandler userInteractionHandler;
    private final TvdbApi api;

    public TvdbAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        this.manager = manager;
        this.userInteractionHandler = userInteractionHandler;
        this.api = new TvdbApi(manager, API_KEY);
    }

    public @Nullable MovieBaseRecord searchMovie(String title) {
        //TODO implement this
        return null;
    }

    public @Nullable TvdbSerie searchSerie(String serieName, ProviderIds providerIds) {
        String encodedSerieName = URLEncoder.encode(serieName.toLowerCase().replace(" ", "-"), StandardCharsets.UTF_8);

        CacheKey cache = getCache(VideoType.EPISODE + "mapping", b -> b
            .addIdParam("name", encodedSerieName)
            .addIdParam("imdbid", providerIds.get(IMDB)));

        if (cache.isPresent() && (!cache.isTemporaryObject() || !cache.isExpiredTemporary())) {
            return cache.get();
        }

        TvdbSerie tvdbSerie = providerIds.get(IMDB, (String imdbId) -> {
            try {
                return new TvdbSerie(serieName, api.searchSerieWithRemoteId(imdbId));
            } catch (TvdbException e) {
                List<SearchResult> serieIds;
                try {
                    serieIds = api.searchSeries(encodedSerieName);
                } catch (TvdbException e2) {
                    serieIds = List.of();
                }
                if (serieIds.isEmpty()) {
                    return null;
                } else if (!userInteractionHandler.settings.optionsConfirmProviderMapping && serieIds.size() == 1) {
                    return new TvdbSerie(serieName, serieIds.first);
                } else {
                    Comparator<SearchResult> comparator = Comparator.comparing(
                        (SearchResult s) -> ProviderId.calculateLevenshteinDistance(serieName, requireNonNull(s.name)));
                    return userInteractionHandler.selectFromList(serieIds.stream().sorted(comparator).toList(),
                            getText("Prompter.SelectTvdbMatchForSerie", serieName), provider,
                            s -> s.name + Stream.of(s.firstAirTime, s.tvdbId).mapFilterNonNull(StringUtils::trimToNull)
                                .collect(Collectors.joining(" - ", "(", ")")))
                        .map(searchResult -> new TvdbSerie(serieName, searchResult))
                        .orElse(null);
                }
            }
        });
        if (tvdbSerie == null) {
            try {
                tvdbSerie = promptUserToEnterTvdbId(serieName).mapToObjEx(api::searchSerieWithTvdbId)
                    .map(serieBaseRecord -> new TvdbSerie(serieName, serieBaseRecord)).orElse(null);
            } catch (TvdbApiException e) {
                //continue
            }
        }
        if (tvdbSerie == null) {
            cache.storeTempValue(Value.nullValue());
        } else {
            cache.store(Value.of(tvdbSerie));
        }
        return tvdbSerie;
    }

    public @Nullable TvdbEpisode searchEpisode(int tvdbId, int season, int episode) {
        return getCache("episode",
            b -> b.add("tvdbId", tvdbId).add("season", season).add("episode", episode))
            .get(
                () -> {
                    try {
                        return api.searchEpisode(tvdbId, season, episode, Language.ENGLISH);
                    } catch (TvdbApiException e) {
                        LOGGER.error(
                            "API $provider getEpisode for serie id [$tvdbId] %s (${e.getMessage()})".formatted(
                                TvRelease.formatSeasonEpisode(season, episode)), e);
                        return null;
                    }
                },
                storeTempNullValue:true);
    }

    public static synchronized TvdbAdapter getInstance(Manager manager, UserInteractionHandler userInteractionHandler) {
        return INSTANCE.apply(manager, userInteractionHandler);
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
