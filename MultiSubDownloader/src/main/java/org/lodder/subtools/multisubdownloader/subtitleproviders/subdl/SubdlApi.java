package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl;

import java.util.List;
import java.util.stream.IntStream;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.exception.SubDlException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSerieId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSubtitleMetadata;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import subdl.Serie;
import subdl.Serie.ReleaseType;

/**
 * An implementation of the {@link SubtitleApi} interface for interacting with the SubDL subtitle provider.
 * It allows fetching subtitle identifiers and subtitle metadata from the SubDL API.
 */
public class SubdlApi implements SubtitleApi {

    private static final String DOMAIN = "https://dl.subdl.com";
    private static final String API_DOMAIN = "https://api.subdl.com/api/v1";
    private static final String API_KEY = "waSZhdBr08sBm3jXNOU0rJ6UWp4lPQvi";
    private final Manager manager;
    @val @override SubtitleSource subtitleSource = SubtitleSource.SUBDL;

    public SubdlApi(Manager manager) {
        this.manager = manager;
    }

    /**
     * Fetches subtitle provider ids from SubDL using either an IMDb ID or a release name.
     * Results are cached to disk.
     *
     * @param imdbId an optional IMDb ID to use for lookup
     * @param name the name of the release or movie/show
     * @return a list of matching {@link SubdlSerieId} objects
     * @throws SubDlException if the API call fails
     */
    public List<SubdlSerieId> getProviderIds(@Nullable Integer imdbId, String name) throws SubDlException {
        return manager.getCache(CacheType.DISK, "$subtitleSource-providerid-$imdbId-$name")
            .getCollection(() -> {
                List<SubdlSerieId> results;
                if (imdbId != null) {
                    results = getProviderIdByImdbId(imdbId);
                    if (!results.isEmpty()) {
                        return results;
                    }
                }
                results = getProviderIdByReleaseName(name);
                if (!results.isEmpty()) {
                    return results;
                }
                return List.of();
            });
    }

    /**
     * Fetches provider IDs based on a release name.
     *
     * @param name the release name to search for
     * @return a list of matching {@link SubdlSerieId} objects
     * @throws SubDlException if the API call fails
     */
    private List<SubdlSerieId> getProviderIdByReleaseName(String name) throws SubDlException {
        return manager.getCache(CacheType.DISK, "$subtitleSource-provideridByReleaseName-" + name)
            .getCollection(() -> {
                try {
                    return Serie.request(API_DOMAIN)
                        .withParam("api_key", API_KEY)
                        .withParam("film_name ", name)
                        .getOne("/subtitles").results.stream().map(this::resultsToProviderId).toList();
                } catch (Exception e) {
                    throw new SubDlException(e);
                }
            });
    }

    /**
     * Fetches provider IDs using an IMDb ID.
     *
     * @param imdbId the IMDb ID
     * @return a list of matching {@link SubdlSerieId} objects
     * @throws SubDlException if the API call fails
     */
    private List<SubdlSerieId> getProviderIdByImdbId(int imdbId) throws SubDlException {
        return manager.getCache(CacheType.DISK, "$subtitleSource-provideridByImdbId-" + imdbId)
            .getCollection(() -> {
                try {
                    return Serie.request(API_DOMAIN)
                        .withParam("api_key", API_KEY)
                        .withParam("imdb_id ", String.valueOf(imdbId))
                        .getOne("/subtitles").results.stream().map(this::resultsToProviderId).toList();
                } catch (Exception e) {
                    throw new SubDlException(e);
                }
            });
    }

    /**
     * Maps a {@link Serie.ResultItem} to a {@link SubdlSerieId}.
     *
     * @param resultItem the result item to convert
     * @return a new {@code SubdlSerieId} instance
     */
    private SubdlSerieId resultsToProviderId(Serie.ResultItem resultItem) {
        return new SubdlSerieId(resultItem.name, String.valueOf(resultItem.sd_id),
            resultItem.year == null ? null : Integer.parseInt(resultItem.year), resultItem.type);
    }

    /**
     * Fetches a list of available subtitles for a given series, season, episode, language.
     * Results are cached in memory.
     *
     * @param sdId the SubDL ID
     * @param season the season number
     * @param episode the episode number
     * @param language the subtitle language
     * @return a list of {@link SubdlSubtitleMetadata} objects matching the given criteria, or an empty list if none
     * @throws SubDlException if the API call fails
     */
    public List<SubdlSubtitleMetadata> getSubtitles(String sdId, int season, int episode,
        Language language) throws SubDlException {
        return manager.getCache(CacheType.MEMORY, "$subtitleSource-subtitles-$sdId-$season-$language-$releaseType")
            .getCollection(() -> {
                try {
                    return Serie.request(API_DOMAIN)
                        .withParam("api_key", API_KEY)
                        .withParam("sd_id", sdId)
                        .withParam("season_number", String.valueOf(season))
//                        .withParam("episode_number", String.valueOf(episode))
                        .withParam("languages", SubdlLanguage.fromLanguage(language).langCode)
                        .withParam("type", ReleaseType.tv.toString())
                        .getOne("/subtitles")
                        .subtitles.stream().map(this::convertToSubtitleMetadata).toList();
                } catch (Exception e) {
                    throw new SubDlException(e);
                }
            }).stream().filter(sub -> sub.episodes.contains(episode)).toList();
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
        return new SubdlSubtitleMetadata(sub.release_name, sub.name, DOMAIN + sub.url, sub.season, episodes,
            sub.author, sub.hi);
    }
}
