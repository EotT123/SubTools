package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.exception.TvSubtitlesException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model.TVsubtitlesSubtitleDescriptor;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.data.Html;
import org.lodder.subtools.sublibrary.data.ProviderSerieId;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

public class JTVSubtitlesApi extends Html implements SubtitleApi {

    private static final String DOMAIN = "https://www.tvsubtitles.net";
    private static final String SERIE_URL_PREFIX = DOMAIN + "/";
    @val @override SubtitleSource subtitleSource = SubtitleSource.TVSUBTITLES;

    public JTVSubtitlesApi(Manager manager) {
        super(manager);
    }

    public List<ProviderSerieId> getUrisForSerieName(String serieName) throws TvSubtitlesException {
        try {
            return manager.postBuilder("$DOMAIN/search.php")
                .addData("qs", serieName)
                .postAsJsoupDocument()
                .select(".left_articles > ul > li a")
                .stream()
                .map(element -> new ProviderSerieId(element.text(),
                    StringUtils.substringAfterLast(element.attr("href"), "/")))
                .toList();
        } catch (Exception e) {
            throw new TvSubtitlesException(e);
        }
    }

    public Set<TVsubtitlesSubtitleDescriptor> getSubtitles(SerieMapping providerSerieId, int season, int episode,
        Language language) throws TvSubtitlesException {
        return getEpisodeUrl(SERIE_URL_PREFIX + providerSerieId.providerId, season, episode).mapThrowing(
            (String episodeUrl) -> getSubtitles(episodeUrl, language)).orElseGet(Set::of);
    }

    private Set<TVsubtitlesSubtitleDescriptor> getSubtitles(String episodeUrl, Language language)
        throws TvSubtitlesException {
        return manager.valueBuilder()
            .memoryCache()
            .key("%s-subtitles-%s-%s".formatted(subtitleSource.name(), episodeUrl, language))
            .collectionSupplier(TVsubtitlesSubtitleDescriptor.class, () -> {
                Set<TVsubtitlesSubtitleDescriptor> lSubtitles = new HashSet<>();
                try {
                    Elements searchEpisodes =
                        manager.getAsJsoupDocument(PageContentParams.params(
                                url:episodeUrl.replace(".html", "-" + language.langCode + ".html"),
                                userAgent:""))
                            .selectAllByCss(".left_articles > a");
                    BiPredicate<Elements, String> isRowWithText = (row, text) -> row.get(1).text().contains(text);
                    Function<Elements, String> getRowValue = row -> row.get(2).text();
                    for (Element ep : searchEpisodes) {
                        String url = ep.attr("href");
                        if (!url.contains("subtitle-")) {
                            continue;
                        }
                        Document subtitlePageDoc = manager.getAsJsoupDocument(
                            PageContentParams.params(url:DOMAIN + url, userAgent:""));
                        String filename = null;
                        String rip = null;
                        String title = null;
                        String author = null;
                        Elements subtitlePageTableDoc = subtitlePageDoc.selectAllByClass("subtitle1");
                        if (subtitlePageTableDoc.size() != 1) {
                            continue;
                        }
                        for (Element item : subtitlePageTableDoc.getFirst().selectAllByTag("tr")) {
                            Elements row = item.getElementsByTag("td");
                            if (row.size() != 3) {
                                continue;
                            }
                            if (isRowWithText.test(row, "episode title:")) {
                                title = getRowValue.apply(row);
                            } else if (isRowWithText.test(row, "filename:")) {
                                filename = getRowValue.apply(row);
                            } else if (isRowWithText.test(row, "rip:")) {
                                rip = getRowValue.apply(row);
                            } else if (isRowWithText.test(row, "author:")) {
                                author = getRowValue.apply(row);
                            }
                            if (filename != null && rip != null) {
                                TVsubtitlesSubtitleDescriptor sub = TVsubtitlesSubtitleDescriptor.builder()
                                    .filename(filename)
                                    .url("$DOMAIN/files/" + URLEncoder.encode(
                                        filename.replace(title + ".", "")
                                            .replace(".srt", ".zip")
                                            .replace(" - ", "_"), StandardCharsets.UTF_8))
                                    .rip(rip)
                                    .author(author)
                                    .build();
                                lSubtitles.add(sub);
                                rip = null;
                                filename = null;
                                title = null;
                                author = null;
                            }
                        }
                    }
                    return lSubtitles;
                } catch (Exception e) {
                    throw new TvSubtitlesException(e);
                }
            })
            .getCollection();
    }

    private Optional<String> getEpisodeUrl(String showUrl, int season, int episode) throws TvSubtitlesException {
        return manager.valueBuilder()
            .memoryCache()
            .key("%s-episodeUrl-%s-%s-%s".formatted(subtitleSource.name(), showUrl, season, episode))
            .optionalSupplier(() -> {
                try {
                    String formattedSeasonEpisode =
                        season + "x" + (episode < 10 ? "0" + episode : String.valueOf(episode));

                    return manager.getAsJsoupDocument(PageContentParams.params(
                            url:showUrl.replace(".html", "-$season.html"),
                            userAgent:""))
                        .selectFirstById("table5")
                        .selectAllByTag("tr")
                        .stream()
                        .skip(1)
                        .filter(row -> Optional.ofNullable(row.selectFirst("td"))
                            .map(element -> formattedSeasonEpisode.equals(element.text()))
                            .orElse(false))
                        .map(element -> DOMAIN + "/" +
                            element.selectNthByTag("td", 2).selectFirstByTag("a").attr("href"))
                        .findAny();
                } catch (Exception e) {
                    throw new TvSubtitlesException(e);
                }
            })
            .getOptional();
    }
}
