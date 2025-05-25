package org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi;

import static manifold.science.measures.TimeUnit.*;
import static org.lodder.subtools.sublibrary.PageContentParams.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.exception.PodnapisiException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.model.PodnapisiSubtitleMetadata;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model.SubdlSerieId;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.Retry;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.util.UrlBuilder;
import org.lodder.subtools.sublibrary.util.http.HttpClientException;

public class PodnapisiApi implements SubtitleApi {

    private static final String DOMAIN = "https://www.podnapisi.net";
    @val Manager manager;
    @val @override SubtitleSource source = SubtitleSource.PODNAPISI;
    private final String userAgent;

    public PodnapisiApi(Manager manager, String userAgent) {
        this.manager = manager;
        this.userAgent = userAgent;
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
     * @return a list of {@link PodnapisiSubtitleMetadata} objects matching the given criteria, or an empty list if none
     * @throws PodnapisiException if the API call fails
     */
    public List<PodnapisiSubtitleMetadata> getMovieSubtitles(String title, @Nullable Integer year,
        Language language) throws PodnapisiException {
        return getSubtitles(title, language, Map.of(SearchParam.YEAR, year));
    }

    // ===== \\
    // SERIE \\
    // ===== \\

    /**
     * Fetches subtitle provider ids using a serie name.
     * Since Podnapisi does not use unique identifiers for series, the series name itself is used as the identifier.
     *
     * @param name the name of the serie
     * @return a list of matching {@link SubdlSerieId} objects
     * @throws PodnapisiException if the API call fails
     */
    public List<ProviderId> getProviderIdsUsingName(String name) throws PodnapisiException {
        return List.of(new ProviderId(name, name));
    }

    /**
     * Fetches a list of available serie subtitles for a given id, season, episode, language.
     * Results are cached in memory.
     *
     * @param serieName the serie name
     * @param season the season number
     * @param episode the episode number
     * @param language the subtitle language
     * @return a list of {@link PodnapisiSubtitleMetadata} objects matching the given criteria, or an empty list if none
     * @throws PodnapisiException if the API call fails
     */
    public List<PodnapisiSubtitleMetadata> getSerieSubtitles(String serieName, int season, int episode,
        Language language) throws PodnapisiException {
        return getSubtitles(serieName, language, Map.of(SearchParam.SEASON, season, SearchParam.EPISODE, episode));
    }


    // ====== \\
    // COMMON \\
    // ====== \\

    /**
     * Fetches a list of available serie subtitles for a given name, language and other parameters.
     * Results are cached in memory.
     *
     * @param name the name to search for
     * @param language the subtitle language
     * @param paramMap extra search parameters
     * @return a list of {@link PodnapisiSubtitleMetadata} objects matching the given criteria, or an empty list if none
     * @throws PodnapisiException if the API call fails
     */

    private List<PodnapisiSubtitleMetadata> getSubtitles(String name, Language language,
        Map<SearchParam, Object> paramMap) throws PodnapisiException {
        return getCache("subtitles", b -> b.add("name", name).add("language", language).add(paramMap))
            .getCollection(() -> {
                try {
                    UrlBuilder urlBuilder = new UrlBuilder(DOMAIN, "/sl/ppodnapisi/search");
                    urlBuilder.addParam(
                        SearchParam.KEYWORDS.pattern.formatted(URLEncoder.encode(name.trim().toLowerCase(),
                            StandardCharsets.UTF_8)));
//                    if (PODNAPISI_LANGS.containsKey(language)) {
//                        url.append(SearchParam.LANGUAGE_OLD.pattern.formatted(PODNAPISI_LANGS.get(language)));
//                    }
                    urlBuilder.addParam(SearchParam.LANGUAGE.pattern.formatted(language.iso639_1));
                    paramMap.forEach((param, value) -> {
                        if (value != null) {
                            urlBuilder.addParam(param.pattern.formatted(value.toString()));
                        }
                    });
                    urlBuilder.addParam(SearchParam.XML.pattern);

                    return getXml(urlBuilder.build()).selectAllByTag("subtitle")
                        .stream()
                        .map(this::parsePodnapisiSubtitle)
                        .filter(metadata -> StringUtils.isNotBlank(metadata.releaseString))
                        .toList();
                } catch (Exception e) {
                    throw new PodnapisiException(e);
                }
            }, new Retry(1, ex -> ex instanceof HttpClientException e && e.responseCode >= 500, 1 Second));
    }

    // see https://www.podnapisi.net/forum/viewtopic.php?f=62&t=26164#p212652
    private enum SearchParam {
        YEAR("sY=%s"),
        SEASON("sTS=%s&sT=1"),
        MOVIE("sT=0"),
        EPISODE("sTE=%s"),
        XML("sXML=1"), //to enable XML output
        KEYWORDS("sK=%s"),
        LANGUAGE_OLD("sJ=%s"), //(old integer IDs), comma delimited
        LANGUAGE("sL=%s"), // in ISO codes (exception are sr-latn and pt-br), comma delimited
        OMDB("sM=%s"), // OMDb movie ID (old numeric ID)
        IMDB("sI=%s"), //
        HEARING_IMPAIRED("sEH=%s"),
        MOVIE_HASH_EXACT("sEH=%s"), // (OSH)
        MOVIE_HASH("sMH=%s"), // (OSH)
        PAGE("page=%s");

        @val String pattern;

        SearchParam(String pattern) {
            this.pattern = pattern;
        }
    }

    protected @Nullable Document getXml(String url) throws PodnapisiException {
        try {
            return manager.getAsJsoupDocument(params(url, CacheType.MEMORY, userAgent,
                new Retry(
                    1,
                    ex -> ex instanceof HttpClientException e && e.responseCode >= 500 &&
                        e.responseCode < 600,
                    5 Second)));
        } catch (Exception e) {
            throw new PodnapisiException(e);
        }
    }

//    protected @Nullable String get(String url) throws PodnapisiException {
//        try {
//            return manager.get(params(url, CacheType.MEMORY, userAgent,
//                new Retry(
//                    1,
//                    ex -> ex instanceof HttpClientException e && e.responseCode >= 500 &&
//                        e.responseCode < 600,
//                    5 Second)));
//        } catch (Exception e) {
//            throw new PodnapisiException(e);
//        }
//    }

    private PodnapisiSubtitleMetadata parsePodnapisiSubtitle(Element elem) {
        Function<Element, String> getText = e -> e == null ? null : e.text();
        return new PodnapisiSubtitleMetadata(
            subtitleId:elem.selectFirst("id").text(),
            name:elem.selectFirst("title").text(),
            imdb:getText.apply(elem.selectFirst("imdb")),
            language:Language.ofIso639_1(elem.selectFirst("languageName").text()),
            uploaderName:elem.selectFirst("uploaderName").text(),
            releaseString:elem.selectFirst("release").text().length() > 10 ? elem.selectFirst("release").text() :
                elem.selectFirst("title").text().replace(":", "") + " " + elem.selectFirst("release").text(),
            url:elem.selectFirst("url").text() + "/download?",
            hearingImpaired:elem.select("new_flags flags")
                .stream()
                .anyMatch(flagElem -> "hearing_impaired".equals(flagElem.text())),
            year:getText.apply(elem.selectFirst("year")),
            rating:Double.parseDouble(elem.selectFirst("rating").text()));
    }

//    private Language languageIdToLanguage(String languageId) {
//        return PODNAPISI_LANGS.entrySet()
//            .stream()
//            .filter(entry -> entry.getValue().equals(languageId))
//            .map(Entry::getKey)
//            .findFirst()
//            .orElse(null);
//    }

//    private static final Map<Language, String> PODNAPISI_LANGS =
//        Collections.unmodifiableMap(new EnumMap<>(Language.class) {
//            @Serial private static final long serialVersionUID = 1L;
//
//            {
//                put(Language.SLOVENIAN, "1");
//                put(Language.ENGLISH, "2");
//                put(Language.NORWEGIAN, "3");
//                put(Language.KOREAN, "4");
//                put(Language.GERMAN, "5");
//                put(Language.ICELANDIC, "6");
//                put(Language.CZECH, "7");
//                put(Language.FRENCH, "8");
//                put(Language.ITALIAN, "9");
//                put(Language.BOSNIAN, "10");
//                put(Language.JAPANESE, "11");
//                put(Language.ARABIC, "12");
//                put(Language.ROMANIAN, "13");
//                put(Language.SPANISH, "14"); // es-ar Spanish (Argentina)
//                put(Language.HUNGARIAN, "15");
//                put(Language.GREEK, "16");
//                put(Language.CHINESE_SIMPLIFIED, "17");
//                put(Language.LITHUANIAN, "19");
//                put(Language.ESTONIAN, "20");
//                put(Language.LATVIAN, "21");
//                put(Language.HEBREW, "22");
//                put(Language.DUTCH, "23");
//                put(Language.DANISH, "24");
//                put(Language.SWEDISH, "25");
//                put(Language.POLISH, "26");
//                put(Language.RUSSIAN, "27");
//                put(Language.SPANISH, "28");
//                put(Language.ALBANIAN, "29");
//                put(Language.TURKISH, "30");
//                put(Language.FINNISH, "31");
//                put(Language.PORTUGUESE, "32");
//                put(Language.BULGARIAN, "33");
//                put(Language.MACEDONIAN, "35");
//                put(Language.SLOVAK, "37");
//                put(Language.CROATIAN, "38");
//                put(Language.CHINESE_SIMPLIFIED, "40");
//                put(Language.HINDI, "42");
//                put(Language.THAI, "44");
//                put(Language.UKRAINIAN, "46");
//                put(Language.SERBIAN, "47");
//                put(Language.PORTUGUESE, "48"); // Portuguese (Brazil)
//                put(Language.IRISH, "49");
//                put(Language.BELARUSIAN, "50");
//                put(Language.VIETNAMESE, "51");
//                put(Language.PERSIAN, "52");
//                put(Language.CATALAN, "53");
//                put(Language.INDONESIAN, "54");
//
//            }
//        });

}
