package org.lodder.subtools.multisubdownloader.subtitleproviders;

import static manifold.science.measures.TimeUnit.*;
import static manifold.science.util.UnitConstants.*;
import static org.lodder.subtools.multisubdownloader.Messages.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.ext.rt.api.Self;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKey;
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.Manager.Value;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.data.AdapterIntf;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.settings.model.MovieMapping;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <API_SUB> type of the subtitle objects returned by the api
 * @param <SUB> type of the converted subtitle objects
 * @param <S_ID> type of the ProviderId
 * @param <X> type of the exception thrown by the api
 */
@ExtensionMethod({Files.class})
public abstract class SubtitleAdapter<API_SUB, SUB extends Subtitle, S_ID extends ProviderId, X extends Exception>
    implements SubtitleProvider<SUB>, AdapterIntf {
    Logger LOGGER = LoggerFactory.getLogger(SubtitleAdapter.class);

    @val @override Manager manager;
    @val UserInteractionHandler userInteractionHandler;
    @val @override String provider = source.name();
    @val boolean useSeasonForSerieId;

    protected SubtitleAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        this.manager = manager;
        this.userInteractionHandler = userInteractionHandler;
    }

    // ====== \\
    // MOVIES \\
    // ====== \\

    public Set<SUB> searchSubtitles(MovieRelease movieRelease, Language language) {
        Set<API_SUB> subtitles = new HashSet<>();
        if (StringUtils.isNotBlank(movieRelease.fileName)) {
            Path file = movieRelease.getPath().resolve(movieRelease.fileName);
            if (file.exists()) {
                try {
                    subtitles.addAll(searchMovieSubtitlesWithHash(FileHasher.computeHash(file), language));
                } catch (IOException e) {
                    LOGGER.error("Error calculating file hash", e);
                } catch (Exception e) {
                    LOGGER.error("API $provider searchSubtitles using file hash for movie [%s] (%s)"
                        .formatted(movieRelease.name, e.getMessage()), e);
                }
            }
        }
        try {
            subtitles.addAll(searchMovieSubtitlesWithId(movieRelease.providerIds, language));
        } catch (Exception e) {
            LOGGER.error("API $provider searchSubtitles using id for movie [%s] (%s)"
                .formatted(movieRelease.name, e.getMessage()), e);
        }
        if (subtitles.isEmpty()) {
            try {
                subtitles.addAll(searchMovieSubtitlesWithName(movieRelease.name, movieRelease.year, language));
            } catch (Exception e) {
                LOGGER.error("API $provider searchSubtitles using title for movie [%s] (%s)"
                    .formatted(movieRelease.name, e.getMessage()), e);
            }
        }
        return subtitles.stream().map(this::convertToSubtitle).toSet();
    }

    public abstract Collection<API_SUB> searchMovieSubtitlesWithHash(String hash, Language language) throws X;

    public abstract Collection<API_SUB> searchMovieSubtitlesWithId(ProviderIds providerIds, Language language) throws X;

    public abstract Collection<API_SUB> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language) throws X;

    /**
     * Attempts to retrieve a movie mapping from a provider using the specified parameters.
     * <p>
     * This method caches the results to prevent redundant provider queries. If no movie mapping is found or if
     * the user is manually searching with a custom name, it will store temporary cache values to avoid unnecessary
     * repeated user prompts during the same execution.
     * </p>
     * <p>
     * If {@code nameToSearchFor} differs from {@code name}, it indicates that the user has entered a custom search name.
     * This distinction is used to determine caching behavior and result matching logic.
     * </p>
     *
     * @param name the name of the movie
     * @param nameToSearchFor the name to search for in the provider's data. If this differs from the name, it is a
     * custom name entered by the user.
     * @param displayName the name to display in the UI
     * @param year the year to narrow down the search results
     * @param providerIds a container of provider-specific identifiers (e.g., TVDB, IMDb)
     * @return an {@code Optional<MovieMapping>} containing the mapping information if found, or an empty {@code
     * Optional} if none is found.
     * @throws X if an error occurs during the retrieval operation
     */
    public Optional<MovieMapping> getProviderMovieMapping(String name, String nameToSearchFor, String displayName,
        @Nullable Integer year, ProviderIds providerIds) throws X {

        CacheKey tvdbIdCache = providerIds.getTvdbId().mapToObj(tvdbId ->
                getCache("movieMapping", b -> b.add("tvdbId", tvdbId).add("year", year)))
            .orElse(null);
        if (tvdbIdCache != null && tvdbIdCache.isPresent()) {
            return tvdbIdCache.getOptional();
        }
        CacheKey imdbIdCache = providerIds.getImdbId().map(imdbId ->
                getCache("movieMapping", b -> b.add("imdbId", imdbId).add("year", year)))
            .orElse(null);
        if (imdbIdCache != null && imdbIdCache.isPresent()) {
            return imdbIdCache.getOptional();
        }
        if (StringUtils.isBlank(nameToSearchFor)) {
            return Optional.empty();
        }

        CacheKey movieNameCache = getCache("movieMapping", b -> b.add("name", name).add("year", year));
        if (StringUtils.equals(nameToSearchFor, name) && movieNameCache.isPresent()) {
            if (movieNameCache.isTemporaryObject()) {
                if (!movieNameCache.isExpiredTemporary()) {
                    return movieNameCache.getOptional();
                }
            } else {
                Optional<MovieMapping> movieMapping = movieNameCache.getOptional();
                if (tvdbIdCache != null) {
                    tvdbIdCache.store(Value.of(movieMapping.orElseThrow()));
                }
                if (imdbIdCache != null) {
                    imdbIdCache.store(Value.of(movieMapping.orElseThrow()));
                }
                return movieMapping;
            }
        }

        List<S_ID> providerMovieIds = getSortedMovieProviderIds(providerIds, nameToSearchFor, year);
        if (providerMovieIds.isEmpty()) {
            // If no movie provider ids are found, store a temporary null value in the cache with a 1-day expiration,
            // to avoid repeatedly querying the provider on each method call.
            // If a previously cached null value has expired, store it again with double the previous expiration time.
            movieNameCache.store(
                value:Value.of(new MovieMapping(name, null, null, year)),
                timeToLive:movieNameCache.getTemporaryTimeToLive().map(v -> v * 2).orElse(1 day),
                storeAsTempValue:true,
                storeTempNullValue:true);
            return Optional.empty();
        }

        MovieMapping movieMapping;
        if (!userInteractionHandler.settings.optionsConfirmProviderMapping && providerMovieIds.size() == 1) {
            // If only one movies mapping is found and the user has disabled confirmation for single results,
            // automatically select this mapping as the desired one.
            movieMapping =
                new MovieMapping(name, providerMovieIds.first.id, providerMovieIds.first.name, year);
        } else {
            // If the user didn’t select a movies provider ID (likely because the correct one wasn’t listed),
            // store it temporarily in the memory cache to avoid prompting the user repeatedly during the same session.
            CacheKey previousResultsCache = manager.getCache(CacheType.MEMORY, new CacheKeyBuilder(provider,
                "name-prev-results").add("name", nameToSearchFor).add("year", year));

            Optional<S_ID> uriForMovie;
            // Skip prompting the user if the previous results for this service were identical.
            if (previousResultsCache.isPresent() && providerMovieIds.equals(previousResultsCache.getCollection(null))) {
                uriForMovie = Optional.empty();
            } else {
                // Prompt the user to select the correct provider movie id.
                uriForMovie = userInteractionHandler.selectFromList(
                    providerMovieIds,
                    year != null ?
                        getText("SelectDialog.SelectMovieNameForNameWithSeason", displayName, year) :
                        getText("SelectDialog.SelectMovieNameForName", displayName),
                    provider,
                    this::providerMovieIdToDisplayString);
            }
            if (uriForMovie.isEmpty()) {
                // If the names differ, the user is manually searching using a custom name.
                // If no result is found, avoid caching it, since the same query is unlikely to be reused.
                if (nameToSearchFor.equals(name)) {
                    // If no movie provider id was selected, cache a temporary null value with a 1-day expiration.
                    // If a temporary null value already exists, update it with double the previous expiration time.
                    movieNameCache.store(
                        value:Value.of(new MovieMapping(nameToSearchFor, null, null, year)),
                        timeToLive:movieNameCache.getTemporaryTimeToLive().map(v -> v * 2).orElse(1 day),
                        storeAsTempValue:true,
                        storeTempNullValue:true);
                    previousResultsCache.store(Value.ofCollection(providerMovieIds));
                }
                return Optional.empty();
            }
            // create a movieMapping for the selected value
            movieMapping = new MovieMapping(name, uriForMovie.get().id, uriForMovie.get().name, year);
        }
        // cache the result
        if (tvdbIdCache != null) {
            tvdbIdCache.store(Value.of(movieMapping));
        }
        if (imdbIdCache != null) {
            imdbIdCache.store(Value.of(movieMapping));
        }
        movieNameCache.store(Value.of(movieMapping));

        return Optional.of(movieMapping);
    }

    // ====== \\
    // SERIES \\
    // ====== \\

    public Set<SUB> searchSubtitles(TvRelease tvRelease, Language language) {
        try {
            return getProviderSerieMapping(tvRelease)
                .mapThrowing(mapping -> tvRelease.episodes.stream()
                    .flatMap(episode -> {
                        try {
                            return searchSubtitles(mapping, tvRelease.season, episode, language).stream();
                        } catch (Exception e) {
                            LOGGER.error("API $source searchSubtitles for serie [%s] (%s)".formatted(
                                    TvRelease.formatName(mapping.providerName, tvRelease.season, episode), e.getMessage()),
                                e);
                            return Stream.of();
                        }
                    })
                    .map(this::convertToSubtitle).toSet())
                .orElse(Set.of());
        } catch (Exception e) {
            String displayName = StringUtils.defaultIfBlank(tvRelease.originalName, tvRelease.name);
            LOGGER.error("API %s searchSubtitles for serie [%s] (%s)".formatted(source.name,
                TvRelease.formatName(displayName, tvRelease.season, tvRelease.firstEpisode), e.getMessage()), e);
            return Set.of();
        }
    }

    public abstract Collection<API_SUB> searchSubtitles(SerieMapping serieMapping, int season, int episode,
        Language language) throws X;

    public Optional<SerieMapping> getProviderSerieMapping(TvRelease tvRelease) throws X {
        if (StringUtils.isNotBlank(tvRelease.customName)) {
            return getProviderSerieMapping(tvRelease, tvRelease.originalName, tvRelease.customName);
        } else {
            Optional<SerieMapping> providerSerieId = getProviderSerieMapping(tvRelease, tvRelease.originalName);
            return providerSerieId.isPresent() ? providerSerieId : getProviderSerieMapping(tvRelease, tvRelease.name);
        }
    }

    private Optional<SerieMapping> getProviderSerieMapping(TvRelease tvRelease, String name, String customName=name)
        throws X {
        return getProviderSerieMapping(name, customName, tvRelease.displayName,
            useSeasonForSerieId ? tvRelease.season : null, tvRelease.providerIds);
    }

    /**
     * Attempts to retrieve a serie mapping from a provider using the specified parameters.
     * <p>
     * This method caches the results to prevent redundant provider queries. If no serie mapping is found or if
     * the user is manually searching with a custom name, it will store temporary cache values to avoid unnecessary
     * repeated user prompts during the same execution.
     * </p>
     * <p>
     * If {@code nameToSearchFor} differs from {@code name}, it indicates that the user has entered a custom search name.
     * This distinction is used to determine caching behavior and result matching logic.
     * </p>
     *
     * @param name the name of the serie
     * @param nameToSearchFor the name to search for in the provider's data. If this differs from the name, it is a
     * custom name entered by the user.
     * @param displayName the name to display in the UI
     * @param season the season number to narrow down the search results
     * @param providerIds a container of provider-specific identifiers (e.g., TVDB, IMDb)
     * @return an {@code Optional<SerieMapping>} containing the mapping information if found, or an empty {@code
     * Optional} if none is found.
     * @throws X if an error occurs during the retrieval operation
     */
    public Optional<SerieMapping> getProviderSerieMapping(String name, String nameToSearchFor, String displayName,
        @Nullable Integer season, ProviderIds providerIds) throws X {

        int seasonToUse = useSeasonForSerieId ? season : 0;
        CacheKey tvdbIdCache = providerIds.getTvdbId().mapToObj(tvdbId ->
                getCache("serieMapping", b -> b.add("tvdbId", tvdbId).add("season", seasonToUse)))
            .orElse(null);
        if (tvdbIdCache != null && tvdbIdCache.isPresent()) {
            return tvdbIdCache.getOptional();
        }
        CacheKey imdbIdCache = providerIds.getImdbId().map(imdbId ->
                getCache("serieMapping", b -> b.add("imdbId", imdbId).add("season", seasonToUse)))
            .orElse(null);
        if (imdbIdCache != null && imdbIdCache.isPresent()) {
            return imdbIdCache.getOptional();
        }
        if (StringUtils.isBlank(nameToSearchFor)) {
            return Optional.empty();
        }

        CacheKey serieNameCache = getCache("serieMapping",
            b -> b.add("name", name).add("season", seasonToUse));
        if (StringUtils.equals(nameToSearchFor, name) && serieNameCache.isPresent()) {
            if (serieNameCache.isTemporaryObject()) {
                if (!serieNameCache.isExpiredTemporary()) {
                    return serieNameCache.getOptional();
                }
            } else {
                Optional<SerieMapping> serieMapping = serieNameCache.getOptional();
                if (tvdbIdCache != null) {
                    tvdbIdCache.store(Value.of(serieMapping.orElseThrow()));
                }
                if (imdbIdCache != null) {
                    imdbIdCache.store(Value.of(serieMapping.orElseThrow()));
                }
                return serieMapping;
            }
        }

        List<S_ID> providerSerieIds = getSortedSerieProviderIds(providerIds, nameToSearchFor, seasonToUse);
        if (providerSerieIds.isEmpty()) {
            // If no serie provider ids are found, store a temporary null value in the cache with a 1-day expiration,
            // to avoid repeatedly querying the provider on each method call.
            // If a previously cached null value has expired, store it again with double the previous expiration time.
            serieNameCache.store(
                value:Value.of(new SerieMapping(name, null, null, seasonToUse)),
                timeToLive:serieNameCache.getTemporaryTimeToLive().map(v -> v * 2).orElse(1 day),
                storeAsTempValue:true,
                storeTempNullValue:true);
            return Optional.empty();
        }

        SerieMapping serieMapping;
        if (!userInteractionHandler.settings.optionsConfirmProviderMapping && providerSerieIds.size() == 1) {
            // If only one series mapping is found and the user has disabled confirmation for single results,
            // automatically select this mapping as the desired one.
            serieMapping =
                new SerieMapping(name, providerSerieIds.first.id, providerSerieIds.first.name, seasonToUse);
        } else {
            // If the user didn’t select a series provider ID (likely because the correct one wasn’t listed),
            // store it temporarily in the memory cache to avoid prompting the user repeatedly during the same session.
            CacheKey previousResultsCache = manager.getCache(CacheType.MEMORY, new CacheKeyBuilder(provider,
                "name-prev-results").add("name", nameToSearchFor).add("season", seasonToUse));

            Optional<S_ID> uriForSerie;
            // Skip prompting the user if the previous results for this service were identical.
            if (previousResultsCache.isPresent() && providerSerieIds.equals(previousResultsCache.getCollection(null))) {
                uriForSerie = Optional.empty();
            } else {
                // Prompt the user to select the correct provider serie id.
                uriForSerie = userInteractionHandler.selectFromList(
                    providerSerieIds,
                    useSeasonForSerieId ?
                        getText("SelectDialog.SelectSerieNameForNameWithSeason", displayName, seasonToUse) :
                        getText("SelectDialog.SelectSerieNameForName", displayName),
                    provider,
                    this::providerSerieIdToDisplayString);
            }
            if (uriForSerie.isEmpty()) {
                // If the names differ, the user is manually searching using a custom name.
                // If no result is found, avoid caching it, since the same query is unlikely to be reused.
                if (nameToSearchFor.equals(name)) {
                    // If no serie provider id was selected, cache a temporary null value with a 1-day expiration.
                    // If a temporary null value already exists, update it with double the previous expiration time.
                    serieNameCache.store(
                        value:Value.of(new SerieMapping(nameToSearchFor, null, null, seasonToUse)),
                        timeToLive:serieNameCache.getTemporaryTimeToLive().map(v -> v * 2).orElse(1 day),
                        storeAsTempValue:true,
                        storeTempNullValue:true);
                    previousResultsCache.store(Value.ofCollection(providerSerieIds));
                }
                return Optional.empty();
            }
            // create a serieMapping for the selected value
            serieMapping = new SerieMapping(name, uriForSerie.get().id, uriForSerie.get().name, seasonToUse);
        }
        // cache the result
        if (tvdbIdCache != null) {
            tvdbIdCache.store(Value.of(serieMapping));
        }
        if (imdbIdCache != null) {
            imdbIdCache.store(Value.of(serieMapping));
        }
        serieNameCache.store(Value.of(serieMapping));

        return Optional.of(serieMapping);
    }

    /**
     * Get a sorted list of provider serie ids for the given serie name and season. Results are already cached and
     * should not be cached in the implementing classes.
     *
     * @param providerIds the provider IDs containing various IDs for providers
     * @param serieName the name of the series
     * @param season the season number of the series
     * @return a list of sorted series provider IDs
     * @throws X if an error occurs during the operation
     */
    public abstract List<S_ID> getSortedSerieProviderIds(ProviderIds providerIds, String serieName,
        int season) throws X;


    // ====== \\
    // COMMON \\
    // ====== \\

    public abstract SUB convertToSubtitle(API_SUB subtitle);

    public abstract String providerSerieIdToDisplayString(S_ID providerSerieId);

    @RequiredArgsConstructor
    public static class ExecuteCall<T, X extends Exception> {
        private final ThrowingSupplier<T, X> supplier;
        private String message;
        private int retries = 3;
        private final List<Predicate<X>> retryPredicates = new ArrayList<>();
        private final List<HandleException<T, X>> exceptionHandlers = new ArrayList<>();

        private record HandleException<T, X extends Exception>(Predicate<X> predicate,
                                                               Function<X, T> exceptionFunction) {
        }

        public @Self ExecuteCall<T, X> retryWhenException(Predicate<X> predicate) {
            retryPredicates.add(predicate);
            return this;
        }

        public @Self ExecuteCall<T, X> handleException(Predicate<X> predicate, Function<X, T> exceptionFunction) {
            exceptionHandlers.add(new HandleException<>(predicate, exceptionFunction));
            return this;
        }

        public @Self ExecuteCall<T, X> handleException(Predicate<X> predicate, Supplier<T> supplier) {
            return handleException(predicate, _ -> supplier.get());
        }

        public @Self ExecuteCall<T, X> handleException(Function<X, T> exceptionFunction) {
            return handleException(_ -> true, exceptionFunction);
        }

        public @Self ExecuteCall<T, X> handleException(Supplier<T> supplier) {
            return handleException(_ -> true, _ -> supplier.get());
        }

        public @Self ExecuteCall<T, X> retries(int retries) {
            if (retries <= 0) {
                throw new IllegalStateException("Retries should be greater than 0");
            }
            this.retries = retries;
            return this;
        }

        public @Self ExecuteCall<T, X> message(String message) {
            this.message = message;
            return this;
        }

        @SuppressWarnings("unchecked")
        public T execute() throws X {
            try {
                return supplier.get();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw e;
                }
                X exception = (X) e;
                if (retryPredicates.stream().anyMatch(predicate -> predicate.test(exception))) {
                    if (retries-- == 0) {
                        throw new RuntimeException("Max retries reached when calling %s".formatted(message));
                    }
                    sleep(5 Second);
                    return execute();
                } else {
                    try {
                        return exceptionHandlers.stream()
                            .filter(handleException -> handleException.predicate().test(exception))
                            .findAny()
                            .map(handleException -> handleException.exceptionFunction().apply(exception))
                            .orElseThrow(() -> e);
                    } catch (Exception e1) {
                        throw (X) e1;
                    }
                }
            }
        }
    }
}
