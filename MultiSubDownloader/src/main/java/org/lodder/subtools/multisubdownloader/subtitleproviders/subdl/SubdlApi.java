package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl;

import static org.lodder.subtools.sublibrary.CacheStrategy.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.json.rt.api.Requester;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.exception.SubdlApiException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSerieId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSubtitleMetadata;
import org.lodder.subtools.multisubdownloader.util.MapUtil;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.cache.ProviderCacheKeyParam;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import subdl.Serie;
import subdl.Serie.ReleaseType;

/**
 * An implementation of the {@link SubtitleApi} interface for interacting with the SubDL subtitle provider.
 * It allows fetching subtitle identifiers and subtitle metadata from the SubDL API.
 */
@NullMarked
public class SubdlApi implements SubtitleApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubtitleApi.class);

    private static final String DOMAIN = "https://dl.subdl.com";
    private static final String API_DOMAIN = "https://api.subdl.com/api/v1";
    private static final String API_KEY = "waSZhdBr08sBm3jXNOU0rJ6UWp4lPQvi";
    @val @override Manager manager;
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.SUBDL;

    public SubdlApi(Manager manager) {
        this.manager = manager;
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    /**
     * Fetches a list of available movie subtitles for the given title, year and language.
     * Results are cached in memory.
     *
     * @param title the movie title
     * @param year the year of the movie (nullable)
     * @param language the subtitle language
     * @return a list of {@link SubdlSubtitleMetadata} objects matching the given criteria, or an empty list if none
     * @throws SubdlApiException if the API call fails
     */
    public List<SubdlSubtitleMetadata> getMovieSubtitles(String title, @Nullable Integer year,
        Language language) throws SubdlApiException {
        Map<SearchParam, @Nullable Serializable> params = MapUtil.create(
            SearchParam.FILM_NAME, title,
            SearchParam.YEAR, year,
            SearchParam.TYPE, ReleaseType.movie);
        return getSubtitles(language, params);
    }

    /**
     * Fetches a list of available movie subtitles for a given imdbId and language.
     * Results are cached in memory.
     *
     * @param imdbId the imdb id of the movie
     * @param language the subtitle language
     * @return a list of {@link SubdlSubtitleMetadata} objects matching the given criteria, or an empty list if none
     * @throws SubdlApiException if the API call fails
     */
    public List<SubdlSubtitleMetadata> getMovieSubtitles(String imdbId, Language language) throws SubdlApiException {
        Map<SearchParam, Serializable> params = MapUtil.create(
            SearchParam.IMDB_ID, imdbId,
            SearchParam.TYPE, ReleaseType.movie);
        return getSubtitles(language, params);
    }

    // ===== \\
    // SERIE \\
    // ===== \\

    /**
     * Fetches subtitle provider id using an IMDB ID.
     * Results are cached in memory.
     *
     * @param imdbId an optional IMDb ID to use for lookup
     * @return an optional containing the {@link SubdlSerieId} object if one was found
     * @throws SubdlApiException if the API call fails
     */
    public Optional<SubdlSerieId> getProviderIdUsingImdbId(String imdbId) throws SubdlApiException {
        return getSerie(MapUtil.create(SearchParam.IMDB_ID, imdbId))
            .results.stream().map(SubdlSerieId::new).findFirst();

//        return getCache("providerId", b -> b.add("imdbId", imdbId))
//            .getCollection(() -> {
//                try {
//                    return Serie.request(API_DOMAIN)
//                        .withParam("api_key", API_KEY)
//                        .withParam("imdb_id ", String.valueOf(imdbId))
//                        .getOne("/subtitles").results.stream().map(SubdlSerieId::new).toList();
//                } catch (Exception e) {
//                    throw new SubdlApiException(e);
//                }
//            });
    }

    /**
     * Fetches subtitle provider ids using a serie name.
     * Results are cached in memory.
     *
     * @param serieName the name of the serie
     * @return a list of matching {@link SubdlSerieId} objects
     * @throws SubdlApiException if the API call fails
     */
    public List<SubdlSerieId> getProviderIdsUsingSerieName(String serieName) throws SubdlApiException {
        return getSerie(MapUtil.create(SearchParam.FILM_NAME, serieName))
            .results.stream().map(SubdlSerieId::new).toList();

//        return getCache("providerId", b -> b.add("name", serieName))
//            .getCollection(() -> {
//                try {
//                    return Serie.request(API_DOMAIN)
//                        .withParam("api_key", API_KEY)
//                        .withParam("film_name ", serieName)
//                        .getOne("/subtitles").results.stream().map(SubdlSerieId::new).toList();
//                } catch (Exception e) {
//                    throw new SubdlApiException(e);
//                }
//            });
    }

    /**
     * Fetches a list of available serie subtitles for a given id, season, episode, language.
     * Results are cached in memory.
     *
     * @param imdbId the imdb id
     * @param season the season number
     * @param episode the episode number
     * @param language the subtitle language
     * @return a list of {@link SubdlSubtitleMetadata} objects matching the given criteria, or an empty list if none
     * @throws SubdlApiException if the API call fails
     */
    public List<SubdlSubtitleMetadata> getSerieSubtitlesUsingImdbId(String imdbId, int season, int episode,
        Language language) throws SubdlApiException {
        Map<SearchParam, Serializable> params = MapUtil.create(
            SearchParam.IMDB_ID, imdbId,
            SearchParam.SEASON, season,
            SearchParam.TYPE, ReleaseType.tv);
        return getSubtitles(language, params)
            .stream().filter(sub -> sub.episodes.contains(episode)).toList();
    }

    /**
     * Fetches a list of available serie subtitles for a given id, season, episode, language.
     * Results are cached in memory.
     *
     * @param providerId the SubDL ID
     * @param season the season number
     * @param episode the episode number
     * @param language the subtitle language
     * @return a list of {@link SubdlSubtitleMetadata} objects matching the given criteria, or an empty list if none
     * @throws SubdlApiException if the API call fails
     */
    public List<SubdlSubtitleMetadata> getSerieSubtitles(String providerId, int season, int episode,
        Language language) throws SubdlApiException {
        Map<SearchParam, Serializable> params = MapUtil.create(
            SearchParam.SUBDL_ID, providerId,
            SearchParam.SEASON, season,
            SearchParam.TYPE, ReleaseType.tv);
        return getSubtitles(language, params)
            .stream().filter(sub -> sub.episodes.contains(episode)).toList();
    }

    // ====== \\
    // COMMON \\
    // ====== \\

    /**
     * Fetches a list of available serie subtitles for a given language and extra parameters.
     * Results are cached in memory.
     *
     * @param language the subtitle language
     * @param extraParams additional search parameters
     * @return a list of {@link SubdlSubtitleMetadata} objects matching the given criteria, or an empty list if none
     * @throws SubdlApiException if the API call fails
     */
    private List<SubdlSubtitleMetadata> getSubtitles(Language language,
        Map<SearchParam, @Nullable Serializable> extraParams) throws SubdlApiException {
        extraParams.put(SearchParam.LANGUAGES,
            SubdlLanguage.of(language).map(SubdlLanguage::getLangCode).collect(Collectors.joining(",")));
        return getSerie(extraParams).subtitles.stream().map(this::convertToSubtitleMetadata).toList();

//        return getCache("serieSubtitles", b -> b.add("language", language).add(extraParams))
//            .getCollection(() -> {
//                try {
//                    Requester<Serie> request = Serie.request(API_DOMAIN);
//                    request.withParam(SearchParam.API_KEY.paramName, API_KEY);
//                    request.withParam(SearchParam.LANGUAGES.paramName, SubdlLanguage.of(language).langCode);
//                    extraParams.entrySet().stream().filter(entry -> entry.value != null)
//                        .forEach(entry -> request.withParam(entry.key.paramName,
//                            String.valueOf(entry.value)));
//                    return request.getOne("/subtitles")
//                        .subtitles.stream().map(this::convertToSubtitleMetadata).toList();
//                } catch (Exception e) {
//                    throw new SubdlApiException(e);
//                }
//            });
    }

    /**
     * Fetches the serie for the given search parameters.
     * Results are cached in memory.
     *
     * @param paramMap search parameters
     * @return a list of {@link SubdlSubtitleMetadata} objects matching the given criteria, or an empty list if none
     * @throws SubdlApiException if the API call fails
     */
    private Serie getSerie(Map<SearchParam, @Nullable Serializable> paramMap) throws SubdlApiException {
        List<ProviderCacheKeyParam> params = paramMap.entrySet().stream()
            .map(entry -> new ProviderCacheKeyParam(entry.getKey().name(), entry.getValue())).toList();
        return getCache("serieSubtitles", b -> b.add(params))
            .get(() -> {
                try {
                    Requester<Serie> request = Serie.request(API_DOMAIN);
                    request.withParam(SearchParam.API_KEY.paramName, API_KEY);
                    paramMap.entrySet().stream().filter(entry -> entry.value != null)
                        .forEach(entry -> request.withParam(entry.key.paramName,
                            String.valueOf(entry.value)));
                    Serie serie = request.getOne("/subtitles");
                    if (!serie.status) {
                        throw SubdlApiException.error(String.valueOf(serie.get("error")));
                    }
                    return serie;
                } catch (Exception e) {
                    throw SubdlApiException.error(e, e.getMessage(), CACHE_DISABLED);
                }
            });
    }

    @NullMarked
    private enum SearchParam {
        API_KEY("api_key"),
        SUBDL_ID("sd_id"),
        SEASON("season_number"),
        EPISODE("episode_number"),
        LANGUAGES("languages"),
        TYPE("type"),
        FILM_NAME("film_name"),
        YEAR("year"),
        IMDB_ID("imdb_id");

        @val String paramName;

        SearchParam(String paramName) {
            this.paramName = paramName;
        }
    }

    /**
     * Converts a SubDL subtitle API result into a {@link SubdlSubtitleMetadata}.
     *
     * @param sub the subtitle data returned by the API
     * @return a metadata representing the subtitle metadata
     */
    private SubdlSubtitleMetadata convertToSubtitleMetadata(Serie.Subtitle sub) {
        List<Integer> episodes;
        if (sub.episode_from != null) {
            episodes = IntStream.rangeClosed(sub.episode_from, sub.episode_end).boxed().toList();
        } else {
            episodes = List.of(sub.episode);
        }
        return new SubdlSubtitleMetadata(sub.release_name.split("/").last(), sub.name.split("/").last(),
            DOMAIN + sub.url, sub.season, episodes, sub.author, sub.hi, SubdlLanguage.of(sub.language).language);
    }
}
