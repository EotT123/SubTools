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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.pivovarit.function.ThrowingSupplier;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.ext.rt.api.Self;
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
import org.lodder.subtools.sublibrary.model.ReleaseIds;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.settings.model.MovieMapping;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.lodder.subtools.sublibrary.util.lazy.LazySupplier;
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
        movieRelease.releaseIds.imdbId.ifPresent(imdbId -> {
            try {
                subtitles.addAll(searchMovieSubtitlesWithId(imdbId, language));
            } catch (Exception e) {
                LOGGER.error("API %s searchSubtitles using imdbid [%s] for movie [%s] (%s)".formatted(
                    source.name, imdbId, movieRelease.name, e.getMessage()), e);
            }
        });

        if (movieRelease.imdbId != null) {
            try {
                subtitles.addAll(searchMovieSubtitlesWithId(movieRelease.imdbId, language));
            } catch (Exception e) {
                LOGGER.error("API %s searchSubtitles using imdbid [%s] for movie [%s] (%s)".formatted(
                    source.name, movieRelease.imdbId, movieRelease.name, e.getMessage()), e);
            }
        }
        if (subtitles.isEmpty()) {
            try {
                subtitles.addAll(searchMovieSubtitlesWithName(movieRelease.name, movieRelease.year, language));
            } catch (Exception e) {
                LOGGER.error("API %s searchSubtitles using title for movie [%s] (%s)".formatted(source.name,
                    movieRelease.name, e.getMessage()), e);
            }
        }
        return subtitles.stream().map(this::convertToSubtitle).collect(Collectors.toSet());
    }

    public abstract Collection<API_SUB> searchMovieSubtitlesWithHash(String hash, Language language) throws X;

    public abstract Collection<API_SUB> searchMovieSubtitlesWithId(int tvdbId, Language language) throws X;

    public abstract Collection<API_SUB> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language) throws X;

    public Set<SUB> searchSubtitles(TvRelease tvRelease, Language language) {
        try {
            return searchSerieSubtitles(tvRelease, language).stream().map(this::convertToSubtitle)
                .collect(Collectors.toSet());
        } catch (Exception e) {
            String displayName = StringUtils.defaultIfBlank(tvRelease.originalName, tvRelease.name);
            LOGGER.error("API %s searchSubtitles for serie [%s] (%s)".formatted(source.name,
                TvRelease.formatName(displayName, tvRelease.season, tvRelease.firstEpisode), e.getMessage()), e);
            return Set.of();
        }
    }

    public abstract Collection<API_SUB> searchSerieSubtitles(TvRelease tvRelease, Language language) throws X;

    public abstract SUB convertToSubtitle(API_SUB subtitle, Language language);

    public abstract List<S_ID> getSortedProviderSerieIds(@Nullable Integer tvdbId, @Nullable Integer imdbId,
        String serieName, int season) throws X;

    public Optional<MovieMapping> getProviderMovieId(MovieRelease movieRelease) throws X {
        LazySupplier<CacheKey> tvdbIdCacheFunction = new LazySupplier<>(() -> manager.getCache(CacheType.DISK,
            "$providerName-movieName-imdbId:$imdbId"));
        if (movieRelease.imdbId != null) {
            CacheKey tvdbIdCache = tvdbIdCacheFunction.get();
            if (tvdbIdCache.isPresent()) {
                // if value using the tvdbId is present, return it
                return tvdbIdCache.getOptional();
            }
        }
        CacheKey movieNameCache = manager.getCache(CacheType.DISK,
            "$providerName-movieName-name:" + movieRelease.name.toLowerCase());
        if (movieNameCache.isPresent()) {
            if (movieNameCache.isTemporaryObject()) {
                if (!movieNameCache.isExpiredTemporary()) {
                    return Optional.empty();
                }
            } else {
                Optional<SerieMapping> serieMapping = movieNameCache.getOptional();
                if (tvdbId != null) {
                    serieMapping.map(Value::of).ifPresent(tvdbIdCacheFunction.get()::store);
                }
                return serieMapping;
            }
        }

        List<S_ID> providerSerieIds = getSortedProviderSerieIds(tvdbId, imdbId, serieNameToSearchFor, seasonToUse);
        if (providerSerieIds.isEmpty()) {
            // If no provider serie id's could be found, store a temporary null value with expiration time of 1 day
            // (so the provider isn't contacted every time this method is being called).
            // If a temporary expired value was already found, persist the null value with a doubled expiration time.
            movieNameCache.store(
                value:Value.of(new SerieMapping(serieName, null, null, seasonToUse)),
                timeToLive:movieNameCache.getTemporaryTimeToLive().map(v -> v * 2).orElse(1 day),
                storeAsTempValue:true,
                storeTempNullValue:true);
            return Optional.empty();
        }

        SerieMapping serieMapping;
        if (!userInteractionHandler.settings.optionsConfirmProviderMapping && providerSerieIds.size() == 1) {
            serieMapping =
                new SerieMapping(serieName, providerSerieIds.first.id, providerSerieIds.first.name, seasonToUse);
        } else {
            CacheKey previousResultsCache = manager.getCache(CacheType.MEMORY,
                "%s-serieName-prev-results:%s-%s".formatted(providerName, displayName.toLowerCase(), seasonToUse));

            boolean previousResultsPresent = previousResultsCache.isPresent();
            Optional<S_ID> uriForSerie;
            // Check if the previous results were the same for the service. If so, don't ask the user to select again
            if (previousResultsPresent && providerSerieIds.equals(previousResultsCache.getCollection(null))) {
                uriForSerie = Optional.empty();
            } else {
                // let the user select the correct provider serie id
                uriForSerie = getUserInteractionHandler().selectFromList(
                    providerSerieIds,
                    useSeasonForSerieId ?
                        getText("SelectDialog.SelectSerieNameForNameWithSeason", displayName, seasonToUse) :
                        getText("SelectDialog.SelectSerieNameForName", displayName),
                    providerName,
                    this::providerSerieIdToDisplayString);
            }
            if (uriForSerie.isEmpty()) {
                if (serieNameToSearchFor.equals(serieName)) {
                    // if no provider serie id was selected, store a temporary null value with expiration time of 1 day,
                    // or the doubled previously temporary value (if present)
                    movieNameCache.store(
                        value:Value.of(new SerieMapping(serieNameToSearchFor, null, null, seasonToUse)),
                        timeToLive:movieNameCache.getTemporaryTimeToLive().map(v -> v * 2).orElse(1 day),
                        storeAsTempValue:true,
                        storeTempNullValue:true);
                    previousResultsCache.store(Value.ofCollection(providerSerieIds));
                }
                return Optional.empty();
            }
            // create a serieMapping for the selected value
            serieMapping = new SerieMapping(serieName, uriForSerie.get().id, uriForSerie.get().name, seasonToUse);
        }
        if (tvdbId != null) {
            tvdbIdCacheFunction.get().store(Value.of(serieMapping));
        } else {
            movieNameCache.store(Value.of(serieMapping));
        }
        return Optional.of(serieMapping);
    }

    public Optional<SerieMapping> getProviderSerieId(TvRelease tvRelease) throws X {
        if (StringUtils.isNotBlank(tvRelease.customName)) {
            return getProviderSerieId(tvRelease, tvRelease.originalName, tvRelease.customName);
        } else {
            Optional<SerieMapping> providerSerieId = getProviderSerieId(tvRelease, tvRelease.originalName);
            return providerSerieId.isPresent() ? providerSerieId : getProviderSerieId(tvRelease, tvRelease.name);
        }
    }

    public Optional<SerieMapping> getProviderSerieId(TvRelease tvRelease, String name) throws X {
        return getProviderSerieId(tvRelease, name, name);
    }

    public Optional<SerieMapping> getProviderSerieId(TvRelease tvRelease, String name, String customName) throws X {
        return getProviderSerieId(name, customName, tvRelease.displayName, tvRelease.season, tvRelease.releaseIds);
    }

    public Optional<SerieMapping> getProviderSerieId(String serieName, String serieNameToSearchFor, String displayName,
        int season, ReleaseIds releaseIds) throws X {

        LazySupplier<CacheKey> tvdbIdCacheFunction = new LazySupplier<>(() -> manager.getCache(CacheType.DISK,
            "%s-serieName-tvdbId:%s-%s".formatted(providerName, tvdbId, useSeasonForSerieId ? season : -1)));
        if (tvdbId != null) {
            CacheKey tvdbIdCache = tvdbIdCacheFunction.get();
            if (tvdbIdCache.isPresent()) {
                // if value using the tvdbId is present, return it
                return tvdbIdCache.getOptional();
            }
        }
        if (StringUtils.isBlank(serieNameToSearchFor)) {
            return Optional.empty();
        }

        int seasonToUse = useSeasonForSerieId ? season : 0;
        CacheKey serieNameCache = manager.getCache(CacheType.DISK,
            "%s-serieName-name:%s-%s".formatted(providerName, serieName.toLowerCase(), seasonToUse));
        if (StringUtils.equals(serieNameToSearchFor, serieName) && serieNameCache.isPresent()) {
            if (serieNameCache.isTemporaryObject()) {
                if (!serieNameCache.isExpiredTemporary()) {
                    return serieNameCache.getOptional();
                }
            } else {
                Optional<SerieMapping> serieMapping = serieNameCache.getOptional();
                if (tvdbId != null) {
                    serieMapping.map(Value::of).ifPresent(tvdbIdCacheFunction.get()::store);
                }
                return serieMapping;
            }
        }

        List<S_ID> providerSerieIds = getSortedProviderSerieIds(tvdbId, imdbId, serieNameToSearchFor, seasonToUse);
        if (providerSerieIds.isEmpty()) {
            // If no provider serie id's could be found, store a temporary null value with expiration time of 1 day
            // (so the provider isn't contacted every time this method is being called).
            // If a temporary expired value was already found, persist the null value with a doubled expiration time.
            serieNameCache.store(
                value:Value.of(new SerieMapping(serieName, null, null, seasonToUse)),
                timeToLive:serieNameCache.getTemporaryTimeToLive().map(v -> v * 2).orElse(1 day),
                storeAsTempValue:true,
                storeTempNullValue:true);
            return Optional.empty();
        }

        SerieMapping serieMapping;
        if (!userInteractionHandler.settings.optionsConfirmProviderMapping && providerSerieIds.size() == 1) {
            serieMapping =
                new SerieMapping(serieName, providerSerieIds.first.id, providerSerieIds.first.name, seasonToUse);
        } else {
            CacheKey previousResultsCache = manager.getCache(CacheType.MEMORY,
                "%s-serieName-prev-results:%s-%s".formatted(providerName, displayName.toLowerCase(), seasonToUse));

            boolean previousResultsPresent = previousResultsCache.isPresent();
            Optional<S_ID> uriForSerie;
            // Check if the previous results were the same for the service. If so, don't ask the user to select again
            if (previousResultsPresent && providerSerieIds.equals(previousResultsCache.getCollection(null))) {
                uriForSerie = Optional.empty();
            } else {
                // let the user select the correct provider serie id
                uriForSerie = getUserInteractionHandler().selectFromList(
                    providerSerieIds,
                    useSeasonForSerieId ?
                        getText("SelectDialog.SelectSerieNameForNameWithSeason", displayName, seasonToUse) :
                        getText("SelectDialog.SelectSerieNameForName", displayName),
                    providerName,
                    this::providerSerieIdToDisplayString);
            }
            if (uriForSerie.isEmpty()) {
                if (serieNameToSearchFor.equals(serieName)) {
                    // if no provider serie id was selected, store a temporary null value with expiration time of 1 day,
                    // or the doubled previously temporary value (if present)
                    serieNameCache.store(
                        value:Value.of(new SerieMapping(serieNameToSearchFor, null, null, seasonToUse)),
                        timeToLive:serieNameCache.getTemporaryTimeToLive().map(v -> v * 2).orElse(1 day),
                        storeAsTempValue:true,
                        storeTempNullValue:true);
                    previousResultsCache.store(Value.ofCollection(providerSerieIds));
                }
                return Optional.empty();
            }
            // create a serieMapping for the selected value
            serieMapping = new SerieMapping(serieName, uriForSerie.get().id, uriForSerie.get().name, seasonToUse);
        }
        if (tvdbId != null) {
            tvdbIdCacheFunction.get().store(Value.of(serieMapping));
        } else {
            serieNameCache.store(Value.of(serieMapping));
        }
        return Optional.of(serieMapping);
    }

    public Optional<SerieMapping> getProviderId(String name, ReleaseIds releaseIds,
        VideoType videoType, UnaryOperator<CacheKeyBuilder> cacheKeyBuilderConsumer=b -> b) throws X {

        Map<String, CacheKey> releaseIdCacheKeyMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : releaseIds.getNonNullIds()) {
            CacheKey cacheKey = manager.getCache(CacheType.DISK,
                new CacheKeyBuilder(source, "providerId")
                    .add("videoType", videoType)
                    .add(entry.key, entry.value));
            if (cacheKey.isPresent()) {
                return cacheKey.getOptional();
            }
            releaseIdCacheKeyMap.put(entry.key, cacheKey);
        }

        int seasonToUse = useSeasonForSerieId ? season : 0;
        CacheKey releaseNameCache = manager.getCache(CacheType.DISK,
            cacheKeyBuilderConsumer.apply(new CacheKeyBuilder(source, "releaseName")
                .add("videoType", videoType)
                .add("name", name.toLowerCase())));

        if (StringUtils.equals(serieNameToSearchFor, serieName) && releaseNameCache.isPresent()) {
            if (releaseNameCache.isTemporaryObject()) {
                if (!releaseNameCache.isExpiredTemporary()) {
                    return releaseNameCache.getOptional();
                }
            } else {
                Optional<SerieMapping> serieMapping = releaseNameCache.getOptional();
                if (tvdbId != null) {
                    serieMapping.map(Value::of).ifPresent(tvdbIdCacheFunction.get()::store);
                }
                return serieMapping;
            }
        }

        List<S_ID> providerSerieIds = getSortedProviderSerieIds(tvdbId, imdbId, serieNameToSearchFor, seasonToUse);
        if (providerSerieIds.isEmpty()) {
            // If no provider serie id's could be found, store a temporary null value with expiration time of 1 day
            // (so the provider isn't contacted every time this method is being called).
            // If a temporary expired value was already found, persist the null value with a doubled expiration time.
            releaseNameCache.store(
                value:Value.of(new SerieMapping(serieName, null, null, seasonToUse)),
                timeToLive:releaseNameCache.getTemporaryTimeToLive().map(v -> v * 2).orElse(1 day),
                storeAsTempValue:true,
                storeTempNullValue:true);
            return Optional.empty();
        }

        SerieMapping serieMapping;
        if (!userInteractionHandler.settings.optionsConfirmProviderMapping && providerSerieIds.size() == 1) {
            serieMapping =
                new SerieMapping(serieName, providerSerieIds.first.id, providerSerieIds.first.name, seasonToUse);
        } else {
            CacheKey previousResultsCache = manager.getCache(CacheType.MEMORY,
                "%s-serieName-prev-results:%s-%s".formatted(providerName, displayName.toLowerCase(), seasonToUse));

            boolean previousResultsPresent = previousResultsCache.isPresent();
            Optional<S_ID> uriForSerie;
            // Check if the previous results were the same for the service. If so, don't ask the user to select again
            if (previousResultsPresent && providerSerieIds.equals(previousResultsCache.getCollection(null))) {
                uriForSerie = Optional.empty();
            } else {
                // let the user select the correct provider serie id
                uriForSerie = getUserInteractionHandler().selectFromList(
                    providerSerieIds,
                    useSeasonForSerieId ?
                        getText("SelectDialog.SelectSerieNameForNameWithSeason", displayName, seasonToUse) :
                        getText("SelectDialog.SelectSerieNameForName", displayName),
                    providerName,
                    this::providerSerieIdToDisplayString);
            }
            if (uriForSerie.isEmpty()) {
                if (serieNameToSearchFor.equals(serieName)) {
                    // if no provider serie id was selected, store a temporary null value with expiration time of 1 day,
                    // or the doubled previously temporary value (if present)
                    releaseNameCache.store(
                        value:Value.of(new SerieMapping(serieNameToSearchFor, null, null, seasonToUse)),
                        timeToLive:releaseNameCache.getTemporaryTimeToLive().map(v -> v * 2).orElse(1 day),
                        storeAsTempValue:true,
                        storeTempNullValue:true);
                    previousResultsCache.store(Value.ofCollection(providerSerieIds));
                }
                return Optional.empty();
            }
            // create a serieMapping for the selected value
            serieMapping = new SerieMapping(serieName, uriForSerie.get().id, uriForSerie.get().name, seasonToUse);
        }
        if (tvdbId != null) {
            tvdbIdCacheFunction.get().store(Value.of(serieMapping));
        } else {
            releaseNameCache.store(Value.of(serieMapping));
        }
        return Optional.of(serieMapping);
    }

    public abstract String providerSerieIdToDisplayString(S_ID providerSerieId);

    @RequiredArgsConstructor
    public static class ExecuteCall<T, X extends Exception> {
        private final ThrowingSupplier<T, X> supplier;
        private String message;
        private int retries = 3;
        private final List<Predicate<X>> retryPredicates = new ArrayList<>();
        private final List<HandleException<T, X>> exceptionHandlers = new ArrayList<>();

        private record HandleException<T, X extends Exception>(Predicate<X> predicate,
            Function<X, T> exceptionFunction) {}

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
