package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles;

import static org.lodder.subtools.sublibrary.CacheStrategy.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Gatherers;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.select.Elements;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.exception.TvSubtitleApiException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model.TVSubtitlesSubtitleMetadata;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.ManagerException;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.control.VideoPatterns.Source;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.util.http.CookieManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Api for retrieving serie information from tvsubtitles.net
 */
@NullMarked
public class TvSubtitlesApi implements SubtitleApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubtitleApi.class);

    private static final String DOMAIN = "https://www.tvsubtitles.net";
    @val @override Manager manager;
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.TVSUBTITLES;

    public TvSubtitlesApi(Manager manager) {
        this.manager = manager;
    }

    public List<ProviderId> getProviderIds(String serieName) throws TvSubtitleApiException {
        return getCache("providerIds", b -> b.add("name", serieName))
            .getCollection(() -> {
                try {
                    return manager.postBuilder("$DOMAIN/search.php")
                        .addData("qs", serieName)
                        .postAsJsoupDocument()
                        .select(".left_articles > ul > li a")
                        .stream()
                        .map(element -> new ProviderId(element.text(),
                            StringUtils.substringAfterLast(element.attr("href"), "/")))
                        .toList();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    throw TvSubtitleApiException.error(e);
                }
            });
    }

    public Set<TVSubtitlesSubtitleMetadata> getSubtitles(String providerId, int season, int episode,
        Language language) throws TvSubtitleApiException {
        Set<TVSubtitlesSubtitleMetadata> results = new HashSet<>();
        TVSubtitlesLanguage providerLang = TVSubtitlesLanguage.of(language).orElse(null);

        Optional<EpisodeRow> episodeRow = getSeasonSubtitleInfo(providerId, season, providerLang)
            .filter(row -> row.isSameEpisode(season, episode)).findAny();
        if (episodeRow.isPresent()) {
            for (String url : episodeRow.get().urls) {
                results.addAll(getSubtitles(url, providerLang));
            }
            return results;
        }
        return results;
    }

    private List<EpisodeRow> getSeasonSubtitleInfo(String providerId, int season,
        @Nullable TVSubtitlesLanguage providerLang) throws TvSubtitleApiException {
        return getCache("seasonSubtitleInfo",
            b -> b.add("providerId", providerId).add("season", season).add("language", providerLang))
            .getCollection(() -> {
                try {
                    CookieManager cookieManager = providerLang == null ? null :
                        new CookieManager().storeCookie("tvsubtitles.net", "setlang", providerLang.langCode);
                    return manager.getAsJsoupDocument(new PageContentParams(
                            DOMAIN + "/" + providerId.replace(".html", "-$season.html"),
                            cookieManager:cookieManager))
                        .select("#table5 tr[bgcolor]")
                        .stream()
                        .filter(episodeRow -> StringUtils.isNotBlank(episodeRow.selectFirst("td").text()))
                        .map(episodeRow -> {
                            Elements tds = episodeRow.select("td");
                            String[] seasonEpisode = tds.get(0).text().split("x");
                            List<String> urls =
                                tds.get(3).select("a").stream().map(elem -> DOMAIN + "/" + elem.attr("href")).toList();
                            return new EpisodeRow(Integer.parseInt(seasonEpisode[0]),
                                Integer.parseInt(seasonEpisode[1]), urls);
                        }).filter(episodeRow -> !episodeRow.urls.isEmpty()).toList();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    throw TvSubtitleApiException.error(e, cacheStrategy:CACHE_DISABLED);
                }
            });
    }

    private List<TVSubtitlesSubtitleMetadata> getSubtitles(String episodeUrl,
        @Nullable TVSubtitlesLanguage providerLang) throws TvSubtitleApiException {
        return getCache("subtitles", b -> b.add("url", episodeUrl))
            .getCollection(() -> {
                try {
                    return manager.getAsJsoupDocument(new PageContentParams(episodeUrl))
                        .select(".left_articles > div[class^='subtitle']")
                        .stream().map(subtitleElement -> {
                            Map<MetadataType, String> metadataMap =
                                subtitleElement.select(".subtitle_grid > div").stream().gather(Gatherers.windowFixed(3))
                                    .map(values -> new Metadata(MetadataType.of(values.get(1).text()),
                                        values.get(2).text()))
                                    .filter(metadata -> metadata.metadataType != null)
                                    .toMap(Metadata::metadataType, Metadata::value);
                            return new TVSubtitlesSubtitleMetadata(
                                metadataMap.get(MetadataType.TITLE),
                                metadataMap.get(MetadataType.FILE_NAME),
                                DOMAIN + "/" + subtitleElement.select("a[href^='download-']").attr("href"),
                                Source.fromValue(metadataMap.get(MetadataType.SOURCE)),
                                metadataMap.get(MetadataType.RELEASE),
                                providerLang != null ? providerLang.language : null);
                        }).toList();
                } catch (ManagerException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw TvSubtitleApiException.error(e, cacheStrategy:CACHE_DISABLED);
                }
            });
    }

    @NullMarked
    private record Metadata(@Nullable MetadataType metadataType, String value) {
    }

    @NullMarked
    private enum MetadataType {
        TITLE("episode title"),
        SOURCE("rip"),
        RELEASE("release"),
        FILE_NAME("filename");

        @val String value;

        MetadataType(String value) {
            this.value = value;
        }

        public static @Nullable MetadataType of(String value) {
            return MetadataType.values().stream().filter(metadataType -> metadataType.value.equals(value)).findAny()
                .orElse(null);
        }
    }

    @NullMarked
    private record EpisodeRow(int season, int episode, List<String> urls) implements Serializable {
        public boolean isSameEpisode(int season, int episode) {
            return this.season == season && this.episode == episode;
        }
    }
}
