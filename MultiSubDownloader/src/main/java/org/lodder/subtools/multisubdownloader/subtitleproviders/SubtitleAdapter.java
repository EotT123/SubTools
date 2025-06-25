package org.lodder.subtools.multisubdownloader.subtitleproviders;

import static manifold.ext.props.rt.api.PropOption.*;
import static manifold.science.util.UnitConstants.*;
import static org.lodder.subtools.multisubdownloader.Messages.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingFunction;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKey;
import org.lodder.subtools.sublibrary.Manager.Value;
import org.lodder.subtools.sublibrary.cache.ProviderCacheKeyParam;
import org.lodder.subtools.sublibrary.data.AdapterIntf;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.settings.model.ReleaseMapping;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.lodder.subtools.sublibrary.util.http.ApiExceptionIntf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <API_SUB> type of the subtitle objects returned by the api
 * @param <SUB> type of the converted subtitle objects
 * @param <S_ID> type of the serie provider id
 * @param <X> type of the exception thrown by the api
 */
//@ExtensionMethod({Files.class})
public abstract class SubtitleAdapter<API_SUB, SUB extends Subtitle, S_ID extends ProviderId, X extends Exception> implements
    SubtitleProvider<SUB>, AdapterIntf {
    Logger LOGGER = LoggerFactory.getLogger(SubtitleAdapter.class);

    @val @override Manager manager;
    @val UserInteractionHandler userInteractionHandler;
    @val(Abstract) boolean useSeasonForSerieId;

    protected SubtitleAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        this.manager = manager;
        this.userInteractionHandler = userInteractionHandler;
    }

    @Override
    public String getProvider() {
        return subtitleProviderFrontEnd.name();
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    public Set<SUB> searchSubtitles(MovieRelease movieRelease, Language language) {
        Set<API_SUB> subtitles = new HashSet<>();
        try {
            subtitles.addAll(searchMovieSubtitlesWithId(movieRelease.providerIds, language));
        } catch (Exception e) {
            LOGGER.error("API $provider searchSubtitles using id for movie [%s] (%s)"
                .formatted(movieRelease.name, e.getMessage()), e);
        }
        if (subtitles.isEmpty()) {
            try {
                subtitles.addAll(searchMovieSubtitlesWithName(movieRelease.name, movieRelease.year, language,
                    movieRelease.providerIds));
            } catch (Exception e) {
                LOGGER.error("API $provider searchSubtitles using title for movie [%s] (%s)"
                    .formatted(movieRelease.name, e.getMessage()), e);
            }
        }
        if (subtitles.isEmpty()) {
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
        }
        return subtitles.stream().map(subtitle -> convertToSubtitle(movieRelease, subtitle)).toSet();
    }

    public abstract Collection<API_SUB> searchMovieSubtitlesWithHash(String hash, Language language) throws X;

    public abstract Collection<API_SUB> searchMovieSubtitlesWithId(ProviderIds providerIds, Language language) throws X;

    // TODO split name and provider ids
    public abstract Collection<API_SUB> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language, ProviderIds providerIds) throws X;


//    @Override
//    public Optional<MovieMapping> getProviderMovieMapping(MovieRelease movieRelease) throws X_API {
//        return getProviderMovieMapping(movieRelease.name, movieRelease.name, movieRelease.name, movieRelease.year,
//            movieRelease.providerIds);
//    }

//    /**
//     * Attempts to retrieve a movie mapping from a provider using the specified parameters.
//     * <p>
//     * This method caches the results to prevent redundant provider queries. If no movie mapping is found or if
//     * the user is manually searching with a custom name, it will store temporary cache values to avoid unnecessary
//     * repeated user prompts during the same execution.
//     * </p>
//     * <p>
//     * If {@code nameToSearchFor} differs from {@code name}, it indicates that the user has entered a custom search name.
//     * This distinction is used to determine caching behavior and result matching logic.
//     * </p>
//     *
//     * @param name the name of the movie
//     * @param nameToSearchFor the name to search for in the provider's data. If this differs from the name, it is a
//     * custom name entered by the user.
//     * @param displayName the name to display in the UI
//     * @param year the year to narrow down the search results
//     * @param providerIds a container of provider-specific identifiers (e.g., TVDB, IMDb)
//     * @return an {@code Optional<MovieMapping>} containing the mapping information if found, or an empty {@code
//     * Optional} if none is found.
//     * @throws X_API if an error occurs during the retrieval operation
//     */
//    private Optional<MovieMapping> getProviderMovieMapping(String name, String nameToSearchFor, String displayName,
//        @Nullable Integer year, ProviderIds providerIds) throws X_API {
//
//        ThrowingBiFunction<ProviderIds, String, List<M_ID>, X_API> providerReleaseIdsFunction
//            = (_providerIds, _nameToSearchFor) -> getSortedMovieProviderIds(_providerIds, _nameToSearchFor, year);
//        TriFunction<String, String, String, MovieMapping> releaseMappingConstructor =
//            (_name, providerId, providerName) -> new MovieMapping(_name, providerId, providerName, year);
//        UnaryOperator<String> selectFromListMessage =
//            _displayName -> year == null ? getText("SelectDialog.SelectMovieNameForName", _displayName) :
//                getText("SelectDialog.SelectMovieNameForNameWithSeason", _displayName, year);
//        Function<M_ID, String> providerReleaseIdToDisplayStringFunction = this::providerMovieIdToDisplayString;
//
//        return getProviderReleaseMapping(name,
//            nameToSearchFor, displayName,
//            Map.of("year", year),
//            providerIds,
//            providerReleaseIdsFunction,
//            releaseMappingConstructor,
//            selectFromListMessage,
//            providerReleaseIdToDisplayStringFunction);
//    }
//
//    /**
//     * Get a sorted list of provider serie ids for the given serie name and season. Results are already cached and
//     * should not be cached in the implementing classes.
//     *
//     * @param providerIds the provider IDs containing various IDs for providers
//     * @param serieName the name of the movie
//     * @param year the year number of the movie
//     * @return a list of sorted movie provider IDs
//     * @throws X_API if an error occurs during the operation
//     */
//    public abstract List<M_ID> getSortedMovieProviderIds(ProviderIds providerIds, String serieName,
//        @Nullable Integer year) throws X_API;
//
//    /**
//     * Converts a provider-specific movie id to a displayable string format.
//     *
//     * @param providerMovieId the provider-specific movie identifier to be converted to a display string
//     * @return a string representation of the movie id suitable for display purposes
//     */
//    public abstract String providerMovieIdToDisplayString(M_ID providerMovieId);

    // ===== \\
    // SERIE \\
    // ===== \\

    public Set<SUB> searchSubtitles(TvRelease tvRelease, Language language) {
        // Search using other provider ids
        List<Collection<API_SUB>> subtitles = tvRelease.episodes.stream().flatMap(episode -> {
            try {
                return searchSubtitles(tvRelease.providerIds, tvRelease.season, episode, language).stream();
            } catch (Exception e) {
                LOGGER.error("API $source searchSubtitles for serie [%s] (%s)".formatted(
                    TvRelease.formatName(tvRelease.name, tvRelease.season, episode), e.getMessage()), e);
                return Stream.empty();
            }
        }).toList();
        if (!subtitles.isEmpty()) {
            return subtitles.stream().flatMap(Collection::stream)
                .map(subtitle -> convertToSubtitle(tvRelease, subtitle)).toSet();
        }
        // Search using current provider id
        try {
            return getProviderSerieMapping(tvRelease)
                .mapEx(mapping -> tvRelease.episodes.stream()
                    .flatMap(episode -> {
                        try {
                            return searchSubtitles(mapping, tvRelease.season, episode, language).stream()
                                .flatMap(Collection::stream);
                        } catch (Exception e) {
                            LOGGER.error("API $source searchSubtitles for serie [%s] (%s)".formatted(
                                    TvRelease.formatName(mapping.providerName, tvRelease.season, episode),
                                    e.getMessage()),
                                e);
                            return Stream.of();
                        }
                    })
                    .map(subtitle -> convertToSubtitle(tvRelease, subtitle)).toSet())
                .orElse(Set.of());
        } catch (Exception e) {
            String displayName = StringUtils.defaultIfBlank(tvRelease.originalName, tvRelease.name);
            LOGGER.error("API %s searchSubtitles for serie [%s] (%s)".formatted(source.name,
                TvRelease.formatName(displayName, tvRelease.season, tvRelease.firstEpisode), e.getMessage()), e);
            return Set.of();
        }
    }

    public abstract Optional<Collection<API_SUB>> searchSubtitles(ProviderIds providerIds, int season, int episode,
        Language language) throws X;

    public abstract Optional<Collection<API_SUB>> searchSubtitles(SerieMapping serieMapping, int season,
        int episode,
        Language language) throws X;

    public Optional<SerieMapping> getProviderSerieMapping(TvRelease tvRelease) throws X {
        if (StringUtils.isNotBlank(tvRelease.customName)) {
            return getProviderSerieMapping(tvRelease, tvRelease.originalName, tvRelease.customName);
        } else {
//            return getProviderSerieMapping(tvRelease, tvRelease.originalName)
//                .orElseMapEx(() -> !Objects.equals(tvRelease.originalName, tvRelease.name) ?
//                    getProviderSerieMapping(tvRelease, tvRelease.name) : Optional.empty());

            Optional<SerieMapping> providerSerieMapping = getProviderSerieMapping(tvRelease, tvRelease.originalName);
            if (providerSerieMapping.isEmpty()) {
                providerSerieMapping =
                    !Objects.equals(tvRelease.originalName, tvRelease.name) ?
                        getProviderSerieMapping(tvRelease, tvRelease.name) : Optional.empty();
            }
            return providerSerieMapping;

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
    private Optional<SerieMapping> getProviderSerieMapping(String name, String nameToSearchFor, String displayName,
        @Nullable Integer season, ProviderIds providerIds) throws X {

        ThrowingSupplier<List<S_ID>, X> providerReleaseIdsByIdFunction =
            () -> getSerieProviderIdById(providerIds, season);
        ThrowingFunction<String, List<S_ID>, X> providerReleaseIdsByNameFunction
            = _nameToSearchFor -> getSortedSerieProviderIds(_nameToSearchFor, season);
        TriFunction<String, String, String, SerieMapping> releaseMappingConstructor =
            (_name, providerId, providerName) -> new SerieMapping(_name, providerId, providerName, season);
        UnaryOperator<String> selectFromListMessage =
            _displayName -> season == null ? getText("SelectDialog.SelectSerieNameForName", _displayName) :
                getText("SelectDialog.SelectSerieNameForNameWithSeason", _displayName, season);
        Function<S_ID, String> providerReleaseIdToDisplayStringFunction = this::providerSerieIdToDisplayString;

        return getProviderReleaseMapping(name,
            nameToSearchFor, displayName,
            List.of(new ProviderCacheKeyParam("season", season)),
            providerIds,
            providerReleaseIdsByIdFunction,
            providerReleaseIdsByNameFunction,
            releaseMappingConstructor,
            selectFromListMessage,
            providerReleaseIdToDisplayStringFunction);
    }

    /**
     * Get a matching provider serie id for the given provider ids. Results are already cached and
     * should not be cached in the implementing classes.
     *
     * @param providerIds the provider IDs containing various IDs for providers
     * @param season the season number of the serie
     * @return a list containing the serie provider IDs found
     * @throws X if an error occurs during the operation
     */
    public abstract List<S_ID> getSerieProviderIdById(ProviderIds providerIds, @Nullable Integer season) throws X;

    /**
     * Get a sorted list of provider serie ids for the given serie name and season. Results are already cached and
     * should not be cached in the implementing classes.
     *
     * @param serieName the name of the serie
     * @param season the season number of the serie
     * @return a list of sorted serie provider IDs
     * @throws X if an error occurs during the operation
     */
    public abstract List<S_ID> getSortedSerieProviderIds(String serieName, @Nullable Integer season) throws X;

    /**
     * Converts a provider-specific serie id to a displayable string format.
     *
     * @param providerSerieId the provider-specific serie identifier to be converted to a display string
     * @return a string representation of the serie id suitable for display purposes
     */
    public abstract String providerSerieIdToDisplayString(S_ID providerSerieId);

// ====== \\
// COMMON \\
// ====== \\


    /**
     * Attempts to retrieve a release mapping from a provider using the specified parameters.
     * <p>
     * This method caches the results to prevent redundant provider queries. If no release mapping is found or if
     * the user is manually searching with a custom name, it will store temporary cache values to avoid unnecessary
     * repeated user prompts during the same execution.
     * </p>
     * <p>
     * If {@code nameToSearchFor} differs from {@code name}, it indicates that the user has entered a custom search name.
     * This distinction is used to determine caching behavior and result matching logic.
     * </p>
     *
     * @param name the name of the release
     * @param nameToSearchFor the name to search for in the provider's data. If this differs from the name, it is a
     * custom name entered by the user.
     * @param displayName the name to display in the UI
     * @param extraParams extra search params to narrow down the search results
     * @param providerIds a container of provider-specific identifiers (e.g., TVDB, IMDb)
     * @param providerReleaseIdsByIdFunction a function that returns an optional provider-specific release IDs for
     * the provided provider ids
     * @param providerReleaseIdsByNameFunction a function that returns a list of provider-specific release IDs for the
     * release name specified by {@code nameToSearchFor}
     * @param releaseMappingConstructor a function that constructs a {@link ReleaseMapping} from the specified name,
     * providerId and providerName.
     * @param selectFromListMessage a function that returns the message to display when the user is prompted to
     * select the correct release provider ID from a list of results.
     * @param providerReleaseIdToDisplayStringFunction a function that converts a provider-specific release ID to a
     * string that is used in the GUI.
     * @param <M> the type of the {@link ReleaseMapping} to return
     * @return an {@code Optional<ReleaseMapping>} containing the mapping information if found, or an empty {@code
     * Optional} if none is found.
     * @throws X if an error occurs during the retrieval operation
     */
    public <M extends ReleaseMapping, P extends ProviderId> Optional<M> getProviderReleaseMapping(String name,
        String nameToSearchFor, String displayName,
        List<ProviderCacheKeyParam> extraParams, ProviderIds providerIds,
        ThrowingSupplier<List<P>, X> providerReleaseIdsByIdFunction,
        ThrowingFunction<String, List<P>, X> providerReleaseIdsByNameFunction,
        TriFunction<String, String, String, M> releaseMappingConstructor,
        UnaryOperator<String> selectFromListMessage,
        Function<P, String> providerReleaseIdToDisplayStringFunction) throws X {

        CacheKey cacheKey = getCache("EPISODEMapping", b -> b
            .addIdParam("tvdbId", providerIds.getTvdbId().mapToObj(v -> v).orElse(null))
            .addIdParam("imdbId", providerIds.getImdbId().orElse(""))
            .addIdParam("name", name)
            .add(extraParams));

        if (cacheKey.isPresent()) {
            return cacheKey.getOptional();
        }

        if (StringUtils.isBlank(nameToSearchFor)) {
            return Optional.empty();
        }

        List<P> providerReleaseIds = providerReleaseIdsByIdFunction.get();
        Function<P, M> providerIdToReleaseMapping =
            providerId -> releaseMappingConstructor.apply(name, providerId.id, providerId.name);
        M releaseMapping = null;
        if (providerReleaseIds.size() == 1) {
            releaseMapping = providerIdToReleaseMapping.apply(providerReleaseIds.first());
        } else if (!providerReleaseIds.isEmpty()) {
            // Prompt the user to select the correct provider release id.
            releaseMapping = userInteractionHandler.selectFromList(
                providerReleaseIds,
                selectFromListMessage.apply(displayName),
                provider,
                providerReleaseIdToDisplayStringFunction).map(providerIdToReleaseMapping).orElse(null);
        }
        if (releaseMapping == null) {
            try {
                providerReleaseIds = providerReleaseIdsByNameFunction.apply(nameToSearchFor);
            } catch (Exception exc) {
                if (exc instanceof ApiExceptionIntf e) {
                    switch (e.cacheStrategy) {
                        case CACHE_DISABLED -> {
                        }
                        case CACHE_TEMPORARY -> cacheKey.storeTempValue(Value.ofOptional(Optional.empty()));
                        case CACHE_PERMANENT -> cacheKey.store(Value.ofOptional(Optional.empty()));
                    }
                }
                throw (X) exc;
            }
            if (!providerReleaseIds.isEmpty()) {
                // If only one releases mapping is found and the user has disabled confirmation for single results,
                // automatically select this mapping as the desired one.
                if (providerReleaseIds.size() == 1 &&
                    !userInteractionHandler.settings.optionsConfirmProviderMapping) {
                    releaseMapping = releaseMappingConstructor.apply(name, providerReleaseIds.first.id,
                        providerReleaseIds.first.name);
                } else {
                    // Prompt the user to select the correct provider release id.
                    releaseMapping = userInteractionHandler.selectFromList(
                            providerReleaseIds,
                            selectFromListMessage.apply(displayName),
                            provider,
                            providerReleaseIdToDisplayStringFunction)
                        .map(providerIdToReleaseMapping).orElse(null);
                }
            }
        }
        if (releaseMapping == null) {
            // If no mapping was found, cache a temporary null value with a 1-day
            // expiration. If a temporary null value already exists, update it with double the previous
            // expiration time.
            cacheKey.store(
                value:Value.ofOptional(Optional.empty()),
                timeToLive:1 day,
                storeTempNullValue:true);
            return Optional.empty();
        } else {
            Optional<M> result = Optional.of(releaseMapping);
            cacheKey.store(Value.ofOptional(result));
            return result;
        }
    }

    public abstract SUB convertToSubtitle(Release release, API_SUB subtitle);
}
