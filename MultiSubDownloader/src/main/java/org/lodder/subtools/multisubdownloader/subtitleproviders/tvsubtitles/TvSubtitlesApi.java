package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.exception.TvSubtitleException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model.TVSubtitlesSubtitleMetadata;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model.TVSubtitlesSubtitleMetadata.TVSubtitlesSubtitleMetadataBuilder;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.ManagerException;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.control.VideoPatterns.Source;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.util.http.CookieManager;

/**
 * Api for retrieving serie information from tvsubtitles.net
 */
public class TvSubtitlesApi implements SubtitleApi {

    private static final String DOMAIN = "https://www.tvsubtitles.net";
    @val @override Manager manager;
    @val @override SubtitleSource source = SubtitleSource.TVSUBTITLES;

    public TvSubtitlesApi(Manager manager) {
        this.manager = manager;
    }

    public List<ProviderId> getProviderIds(String serieName) throws TvSubtitleException {
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
                    throw new TvSubtitleException(e);
                }
            });
    }

    public Set<TVSubtitlesSubtitleMetadata> getSubtitles(String providerId, int season, int episode,
        Language language) throws TvSubtitleException {
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
        @Nullable TVSubtitlesLanguage providerLang) throws TvSubtitleException {
        return getCache("seasonSubtitleInfo",
            b -> b.add("providerId", providerId).add("season", season).add("language", providerLang))
            .getCollection(() -> {
                try {
                    CookieManager cookieManager = providerLang == null ? null :
                            new CookieManager().storeCookie("tvsubtitles.net", "setlang", providerLang.langCode);
                    return manager.getAsJsoupDocument(PageContentParams.params(
                            DOMAIN + "/" + providerId.replace(".html", "-$season.html"),
                            cookieManager:cookieManager))
                        .select("#table4 table tr[bgcolor]")
                        .stream()
                        .filter(episodeRow -> StringUtils.isNotBlank(episodeRow.selectFirstByTag("td").text()))
                        .map(episodeRow -> {
                            Elements tds = episodeRow.selectAllByTag("td");
                            String[] seasonEpisode = tds.get(0).text().split("x");
                            List<String> urls =
                                tds.get(3).select("a").stream().map(elem -> DOMAIN + "/" + elem.attr("href")).toList();
                            return new EpisodeRow(Integer.parseInt(seasonEpisode[0]),
                                Integer.parseInt(seasonEpisode[1]), urls);
                        }).filter(episodeRow -> !episodeRow.urls.isEmpty()).toList();
                } catch (Exception e) {
                    throw new TvSubtitleException(e);
                }
            });
    }

    private List<TVSubtitlesSubtitleMetadata> getSubtitles(String episodeUrl,
        @Nullable TVSubtitlesLanguage providerLang) throws TvSubtitleException {
        return getCache("subtitles", b -> b.add("url", episodeUrl))
            .getCollection(() -> {
                try {
                    return manager.getAsJsoupDocument(PageContentParams.url(episodeUrl))
                        .select(".left_articles > div[class^='subtitle']")
                        .stream().map(subtitleElement -> {
                            TVSubtitlesSubtitleMetadataBuilder subtitleBuilder = TVSubtitlesSubtitleMetadata.builder()
                                .language(providerLang != null ? providerLang.language : null);
                            for (Element titleElement : subtitleElement.select(".subtitle_grid > div > img[title]")) {
                                String value =
                                    ((Element) titleElement.parent()).nextElementSibling().nextElementSibling().text();
                                switch (titleElement.attr("title")) {
                                    case "episode title" -> subtitleBuilder.title(value);
                                    case "rip" -> subtitleBuilder.source(Source.fromValue(value));
                                    case "release" -> subtitleBuilder.releaseGroup(value);
                                    case "filename" -> subtitleBuilder.filename(value);
                                    default -> {
                                    }
                                }
                            }
                            subtitleBuilder.url(DOMAIN + "/" + subtitleElement.select("a[href^='download-']")
                                .attr("href"));
                            return subtitleBuilder.build();
                        }).toList();
                } catch (ManagerException e) {
                    throw new TvSubtitleException(e);
                }
            });
    }

    private record EpisodeRow(int season, int episode, List<String> urls) implements Serializable {
        public boolean isSameEpisode(int season, int episode) {
            return this.season == season && this.episode == episode;
        }
    }
}
