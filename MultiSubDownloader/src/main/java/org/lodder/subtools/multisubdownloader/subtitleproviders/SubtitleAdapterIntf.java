package org.lodder.subtools.multisubdownloader.subtitleproviders;

import static manifold.science.util.UnitConstants.*;
import static org.lodder.subtools.multisubdownloader.Messages.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import lombok.experimental.ExtensionMethod;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager.CacheKey;
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.Manager.Value;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.ProviderIds;
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
public interface SubtitleAdapterIntf<API_SUB, SUB extends Subtitle, S_ID extends ProviderId, X extends Exception> extends
    SubtitleProvider<SUB> {
    Logger LOGGER = LoggerFactory.getLogger(SubtitleAdapterIntf.class);

//    default UserInteractionSettingsIntf getUserInteractionSettings() {
//        return getUserInteractionHandler().settings;
//    }

//    UserInteractionHandler getUserInteractionHandler();

    @Override
    default Set<SUB> searchSubtitles(MovieRelease movieRelease, Language language) {
        Set<API_SUB> subtitles = new HashSet<>();
        if (StringUtils.isNotBlank(movieRelease.fileName)) {
            Path file = movieRelease.getPath().resolve(movieRelease.fileName);
            if (file.exists()) {
                try {
                    subtitles.addAll(searchMovieSubtitlesWithHash(FileHasher.computeHash(file), language));
                } catch (IOException e) {
                    LOGGER.error("Error calculating file hash", e);
                } catch (Exception e) {
                    LOGGER.error("API %s searchSubtitles using file hash for movie [%s] (%s)".formatted(
                        subtitleSource.name, movieRelease.name, e.getMessage()), e);
                }
            }
        }
        if (movieRelease.imdbId != null) {
            try {
                subtitles.addAll(searchMovieSubtitlesWithId(movieRelease.imdbId, language));
            } catch (Exception e) {
                LOGGER.error("API %s searchSubtitles using imdbid [%s] for movie [%s] (%s)".formatted(
                    subtitleSource.name, movieRelease.imdbId, movieRelease.name, e.getMessage()), e);
            }
        }
        if (subtitles.isEmpty()) {
            try {
                subtitles.addAll(searchMovieSubtitlesWithName(movieRelease.name, movieRelease.year, language));
            } catch (Exception e) {
                LOGGER.error("API %s searchSubtitles using title for movie [%s] (%s)".formatted(subtitleSource.name,
                    movieRelease.name, e.getMessage()), e);
            }
        }
        return subtitles.stream().map(this::convertToSubtitle).toSet();
    }

    Collection<API_SUB> searchMovieSubtitlesWithHash(String hash, Language language) throws X;

    Collection<API_SUB> searchMovieSubtitlesWithId(int tvdbId, Language language) throws X;

    Collection<API_SUB> searchMovieSubtitlesWithName(String name, @Nullable Integer year, Language language) throws X;

    @Override
    default Set<SUB> searchSubtitles(TvRelease tvRelease, Language language) {
        try {
            return searchSerieSubtitles(tvRelease, language).stream().map(this::convertToSubtitle).toSet();
        } catch (Exception e) {
            String displayName = StringUtils.defaultIfBlank(tvRelease.originalName, tvRelease.name);
            LOGGER.error("API %s searchSubtitles for serie [%s] (%s)".formatted(subtitleSource.name,
                TvRelease.formatName(displayName, tvRelease.season, tvRelease.firstEpisode), e.getMessage()), e);
            return Set.of();
        }
    }

    Collection<API_SUB> searchSerieSubtitles(TvRelease tvRelease, Language language) throws X;

    SUB convertToSubtitle(API_SUB subtitle, Language language);

    List<S_ID> getSortedProviderSerieIds(@Nullable Integer tvdbId, @Nullable Integer imdbId, String serieName,
        int season) throws X;

    @Override
    default Optional<MovieMapping> getProviderMovieId(MovieRelease movieRelease) throws X {
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
        if (!getUserInteractionSettings().optionsConfirmProviderMapping && providerSerieIds.size() == 1) {
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

    @Override
    default Optional<SerieMapping> getProviderSerieMapping(TvRelease tvRelease) throws X {
        if (StringUtils.isNotBlank(tvRelease.customName)) {
            return getProviderSerieId(tvRelease, tvRelease.originalName, tvRelease.customName);
        } else {
            Optional<SerieMapping> providerSerieId = getProviderSerieId(tvRelease, tvRelease.originalName);
            return providerSerieId.isPresent() ? providerSerieId : getProviderSerieId(tvRelease, tvRelease.name);
        }
    }

    default Optional<SerieMapping> getProviderSerieId(TvRelease tvRelease, String name) throws X {
        return getProviderSerieId(tvRelease, name, name);
    }

    default Optional<SerieMapping> getProviderSerieId(TvRelease tvRelease, String name, String customName) throws X {
        return getProviderSerieId(name, customName, tvRelease.displayName, tvRelease.season, tvRelease.providerIds);
    }

    default Optional<SerieMapping> getProviderSerieId(String serieName, String serieNameToSearchFor, String displayName,
        int season, ProviderIds providerIds) throws X {

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
        if (!getUserInteractionSettings().optionsConfirmProviderMapping && providerSerieIds.size() == 1) {
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

    default Optional<SerieMapping> getProviderId(String name, ProviderIds providerIds,
        VideoType videoType, UnaryOperator<CacheKeyBuilder> cacheKeyBuilderConsumer=b -> b) throws X {

        Map<String, CacheKey> providerIdCacheKeyMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : providerIds.getNonNullIds()) {
            CacheKey cacheKey = manager.getCache(CacheType.DISK,
                new CacheKeyBuilder(source, "providerId")
                    .add("videoType", videoType)
                    .add(entry.key, entry.value));
            if (cacheKey.isPresent()) {
                return cacheKey.getOptional();
            }
            providerIdCacheKeyMap.put(entry.key, cacheKey);
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
        if (!getUserInteractionSettings().optionsConfirmProviderMapping && providerSerieIds.size() == 1) {
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

    @val boolean useSeasonForSerieId;

    String providerSerieIdToDisplayString(S_ID providerSerieId);
}
