package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene;

import static manifold.science.measures.TimeUnit.*;
import static org.lodder.subtools.sublibrary.CacheStrategy.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;
import static org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus.*;
import static util.Utils.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.science.measures.Time;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception.SubsceneApiException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model.SearchResultType;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model.SubSceneSerieId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model.SubsceneSubtitleMetadata;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.Retry;
import org.lodder.subtools.sublibrary.ManagerException;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class SubsceneApi implements SubtitleApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubtitleApi.class);

    private static final Time RATE_DURATION_SHORT = 1Second;
    private static final Time RATE_DURATION_LONG = 5Second;
    private static final String DOMAIN = "https://sub-scene.com";
    private static final Pattern MOVIE_NAME_PATTERN = Pattern.compile("(?<title>.*?) \\((?<year>\\d{4})\\)");
    private static final Pattern SERIE_NAME_PATTERN =
        Pattern.compile("(?<name>.*?) - (?<seasonName>[A-Z][a-z]*) Season.*");

    private static final Predicate<Exception> RETRY_PREDICATE = exception ->
        switch (exception) {
            case HttpClientException e -> e.responseCode == CONFLICT || e.responseCode == TOO_MANY_REQUESTS;
            case ManagerException e -> e.getMessage().contains("409 Conflict");
            default -> false;
        };

    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.SUBSCENE;
    private int selectedLanguage;
    private boolean selectedIncludeHearingImpaired;

    private Time lastRequest = Time.now();

    public SubsceneApi() {
//        super("Mozilla/5.25 Netscape/5.0 (Windows; I; Win95)");
        addCookie("ForeignOnly", "False");
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

//    /**
//     * @param title the movie title
//     * @return a {@link Map} containing a list of {@link ProviderId provider serie ids} per type
//     * @throws SubsceneApiException SubsceneApiException
//     */
//    public Map<SearchResultType, List<SubSceneMovieId>> getMovieProviderIds(String title) throws SubsceneApiException {
//        return getProviderIds(title, elem -> {
//            String _title = null;
//            Integer _year = null;
//            Matcher matcher = MOVIE_NAME_PATTERN.matcher(elem.text());
//            if (matcher.matches()) {
//                _title = matcher.group("title");
//                _year = Integer.parseInt(matcher.group("year"));
//            }
//            return new SubSceneMovieId(elem.text(), elem.attr("href"), _title, _year);
//        });
//    }

    // ===== \\
    // SERIE \\
    // ===== \\

    /**
     * @param searchQuery the name of the serie, or the imdb id
     * @return a {@link Map} containing a list of {@link ProviderId provider serie ids} per type
     * @throws SubsceneApiException SubsceneApiException
     */
    public Map<SearchResultType, List<SubSceneSerieId>> getSerieProviderIds(String searchQuery)
        throws SubsceneApiException {
        return getProviderIds(searchQuery, elem -> {
            Matcher matcher = SERIE_NAME_PATTERN.matcher(elem.text());
            if (matcher.matches()) {
                String name = matcher.group("name");
                Integer season = getOrdinalNumber(matcher.group("seasonName"));
                return new SubSceneSerieId(elem.text(), elem.attr("href"), name, season);
            }
            return new SubSceneSerieId(elem.text(), elem.attr("href"));
        });
    }

    public List<SubsceneSubtitleMetadata> getSubtitles(String providerId, int season, int episode,
        Language language) throws SubsceneApiException {
        return getCache("subtitles",
            b -> b.add("providerId", providerId).add("season", season).add("episode", episode))
            .get(() -> {
                setLanguageWithCookie(language);
                try {
                    return getJsoupDocument(DOMAIN + providerId)
                        .select("td.a1")
                        .stream()
                        .map((Element e) -> e.parentElement())
                        .map(row -> {
                            Language lang = Language.ofName(row.select(".a1 span.l").text().trim(), Language.ENGLISH);
                            String name = row.select(".a1 span:not(.l)").text().trim();
                            boolean hearingImpaired = row.selectFirstByCss(".a41") != null;
                            String uploader = row.selectFirstByCss(".a5 > a").text().trim();
                            String comment = row.selectFirstByCss(".a6 > div").text().trim();
                            ThrowingSupplier<String, SubsceneApiException> urlSupplier = () -> getDownloadUrl(
                                DOMAIN + row.select(".a1 > a").attr("href").trim());
                            return new SubsceneSubtitleMetadata(lang, name, hearingImpaired, uploader, comment,
                                urlSupplier);
                        })
                        .filter(metadata -> metadata.seasonEpisode != null &&
                            metadata.seasonEpisode.containsEpisode(episode))
                        // TODO is this needed?
                        .filter(metadata -> metadata.language == language)
                        // TODO is this needed
                        .filter(sub -> sub.name.contains("S%02dE%02d".formatted(season, episode)))
                        .toList();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    throw SubsceneApiException.error(e, cacheStrategy:CACHE_DISABLED);
                }
            });
    }

    // ====== \\
    // COMMON \\
    // ====== \\

    /**
     * @param name the release name
     * @param <S> the type of the {@link SubSceneSerieId} contained in the {@link Map}
     * @return a {@link Map} containing a list of {@link ProviderId provider release ids} per type
     * @throws SubsceneApiException SubsceneApiException
     */
    public <S extends SubSceneSerieId> Map<SearchResultType, List<S>> getProviderIds(String name,
        Function<Element, S> subsceneIdCreator) throws SubsceneApiException {
        try {
            if (StringUtils.isBlank(name)) {
                return Map.of();
            }
            String url = "$DOMAIN/subtitles/searchbytitle?query=" + name.urlEncode();
            return getJsoupDocument(url).selectFirstByClass("search-result").select("h2")
                .stream()
                .collect(mapCollector((map, titleElement) -> map.put(SearchResultType.of(titleElement.text()),
                    titleElement.nextElementSibling().select("a").stream().map(subsceneIdCreator).toList())));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw SubsceneApiException.error(e);
        }
    }

    private String getDownloadUrl(String seriePageUrl) throws SubsceneApiException {
        try {
            String href = getJsoupDocument(seriePageUrl).selectFirstById("downloadButton").attr("href");
            if (StringUtils.isBlank(href)) {
                throw SubsceneApiException.error(message:"href for $seriePageUrl is blank");
            }
            return DOMAIN + href;
        } catch (ManagerException e) {
            LOGGER.error(e.getMessage(), e);
            throw SubsceneApiException.error(e, cacheStrategy:CACHE_DISABLED);
        }
    }

    private Document getJsoupDocument(String url) throws ManagerException {
        Time timeToSleep = RATE_DURATION_SHORT - Time.now() + lastRequest;
        if (timeToSleep.isPositive) {
            sleep(timeToSleep);
        }

        Document document =
            Manager.getDocument(new PageContentParams(
                url:url,
                userAgent:"",
                retry:new Retry(1, RETRY_PREDICATE, RATE_DURATION_LONG)));
        lastRequest = Time.now();
        return document;
    }

    private void setLanguageWithCookie(Language language) {
        Integer languageId = getSubsceneLangId(language);
        if (languageId != null && selectedLanguage != languageId) {
            addCookie("LanguageFilter", String.valueOf(languageId));
            selectedLanguage = languageId;
        }
    }

    @SuppressWarnings("unused")
    private void setIncludeHearingImpairedWithCookie(boolean includeHearingImpaired) {
        if (selectedIncludeHearingImpaired != includeHearingImpaired) {
            addCookie("HearingImpaired", includeHearingImpaired ? "2" : "0");
            selectedIncludeHearingImpaired = includeHearingImpaired;
        }
    }

    private void addCookie(String cookieName, String cookieValue) {
        Manager.storeCookies("subscene.com", Map.of(cookieName, cookieValue));
    }

    private Integer getSubsceneLangId(Language language) {
        return switch (language) {
            case ARABIC -> 2;
            case BENGALI -> 54;
//             case BRAZILLIAN PORTUGUESE -> 4; // BRAZILLIAN PORTUGUESE
            case CHINESE -> 7; // CHINESE SIMPLIFIED
            case CZECH -> 9;
            case DANISH -> 10;
            case DUTCH_FLEMISH -> 11;
            case ENGLISH -> 13;
            case PERSIAN -> 46;
            case FINNISH -> 17;
            case FRENCH -> 18;
            case GERMAN -> 19;
            case GREEK_MODERN -> 21;
            case HEBREW -> 22;
            case INDONESIAN -> 44;
            case ITALIAN -> 26;
            case KOREAN -> 28;
            case MALAY -> 50;
            case NORWEGIAN -> 30;
            case POLISH -> 31;
            case PORTUGUESE -> 32;
            case ROMANIAN_MOLDAVIAN_MOLDOVAN -> 33;
            case SPANISH_CASTILIAN -> 38;
            case SWEDISH -> 39;
            case THAI -> 40;
            case TURKISH -> 41;
            case VIETNAMESE -> 45;
            case ALBANIAN -> 1;
            case ARMENIAN -> 73;
            case AZERBAIJANI -> 55;
            case BASQUE -> 74;
            case BELARUSIAN -> 68;
//            case CHINESE -> 3; // BIG 5 CODE
            case BOSNIAN -> 60;
            case BULGARIAN -> 5;
//             case BULGARIAN / ENGLISH -> 6;
            case BURMESE -> 61;
            case CENTRAL_KHMER -> 79;
            case CATALAN_VALENCIAN -> 49;
            case CROATIAN -> 8;
            // case DUTCH / ENGLISH -> 12;
            // case ENGLISH / GERMAN -> 15;
            case ESPERANTO -> 47;
            case ESTONIAN -> 16;
            case GEORGIAN -> 62;
            case KALAALLISUT_GREENLANDIC -> 57;
            case HINDI -> 51;
            case HUNGARIAN -> 23;
//             case HUNGARIAN / ENGLISH -> 24;
            case ICELANDIC -> 25;
            case JAPANESE -> 27;
            case KANNADA -> 78;
            case KINYARWANDA -> 81;
            case KURDISH -> 52;
            case LATVIAN -> 29;
            case LITHUANIAN -> 43;
            case MACEDONIAN -> 48;
            case MALAYALAM -> 64;
//             case MANIPURI -> 65;
            case MONGOLIAN -> 72;
            case NEPALI -> 80;
            case PASHTO_PUSHTO -> 67;
            case PUNJABI_PANJABI -> 66;
            case RUSSIAN -> 34;
            case SERBIAN -> 35;
            case SINHALA_SINHALESE -> 58;
            case SLOVAK -> 36;
            case SLOVENIAN -> 37;
            case SOMALI -> 70;
            case SUNDANESE -> 76;
            case SWAHILI -> 75;
            case TAGALOG -> 53;
            case TAMIL -> 59;
            case TELUGU -> 63;
            case UKRAINIAN -> 56;
            case URDU -> 42;
            case YORUBA -> 71;
            default -> null;
        };
    }

    private Integer getOrdinalNumber(String text) {
        return switch (text) {
            case "Zeroth" -> 0;
            case "First" -> 1;
            case "Second" -> 2;
            case "Third" -> 3;
            case "Fourth" -> 4;
            case "Fifth" -> 5;
            case "Sixth" -> 6;
            case "Seventh" -> 7;
            case "Eighth" -> 8;
            case "Ninth" -> 9;
            case "Tenth" -> 10;
            case "Eleventh" -> 11;
            case "Twelfth" -> 12;
            case "Thirteenth" -> 13;
            case "Fourteenth" -> 14;
            case "Fifteenth" -> 15;
            case "Sixteenth" -> 16;
            case "Seventeenth" -> 17;
            case "Eighteenth" -> 18;
            case "Nineteenth" -> 19;
            case "Twentieth" -> 20;
            case "Twenty-First" -> 21;
            case "Twenty-Second" -> 22;
            case "Twenty-Third" -> 23;
            case "Twenty-Fourth" -> 24;
            case "Twenty-Fifth" -> 25;
            case "Twenty-Sixth" -> 26;
            case "Twenty-Seventh" -> 27;
            case "Twenty-Eighth" -> 28;
            case "Twenty-Ninth" -> 29;
            case "Thirtieth" -> 30;
            case "Thirty-First" -> 31;
            case "Thirty-Second" -> 32;
            case "Thirty-Third" -> 33;
            case "Thirty-Fourth" -> 34;
            case "Thirty-Fifth" -> 35;
            case "Thirty-Sixth" -> 36;
            case "Thirty-Seventh" -> 37;
            case "Thirty-Eighth" -> 38;
            case "Thirty-Ninth" -> 39;
            case "Fortieth" -> 40;
            case "Forty-First" -> 41;
            case "Forty-Second" -> 42;
            case "Forty-Third" -> 43;
            case "Forty-Fourth" -> 44;
            case "Forty-Fifth" -> 45;
            case "Forty-Sixth" -> 46;
            case "Forty-Seventh" -> 47;
            case "Forty-Eighth" -> 48;
            case "Forty-Ninth" -> 49;
            case "Fiftieth" -> 50;
            case "Fifty-First" -> 51;
            case "Fifty-Second" -> 52;
            case "Fifty-Third" -> 53;
            case "Fifty-Fourth" -> 54;
            case "Fifty-Fifth" -> 55;
            case "Fifty-Sixth" -> 56;
            case "Fifty-Seventh" -> 57;
            case "Fifty-Eighth" -> 58;
            case "Fifty-Ninth" -> 59;
            case "Sixtieth" -> 60;
            case "Sixty-First" -> 61;
            case "Sixty-Second" -> 62;
            case "Sixty-Third" -> 63;
            case "Sixty-Fourth" -> 64;
            case "Sixty-Fifth" -> 65;
            case "Sixty-Sixth" -> 66;
            case "Sixty-Seventh" -> 67;
            case "Sixty-Eighth" -> 68;
            case "Sixty-Ninth" -> 69;
            case "Seventieth" -> 70;
            case "Seventy-First" -> 71;
            case "Seventy-Second" -> 72;
            case "Seventy-Third" -> 73;
            case "Seventy-Fourth" -> 74;
            case "Seventy-Fifth" -> 75;
            case "Seventy-Sixth" -> 76;
            case "Seventy-Seventh" -> 77;
            case "Seventy-Eighth" -> 78;
            case "Seventy-Ninth" -> 79;
            case "Eightieth" -> 80;
            case "Eighty-First" -> 81;
            case "Eighty-Second" -> 82;
            case "Eighty-Third" -> 83;
            case "Eighty-Fourth" -> 84;
            case "Eighty-Fifth" -> 85;
            case "Eighty-Sixth" -> 86;
            case "Eighty-Seventh" -> 87;
            case "Eighty-Eighth" -> 88;
            case "Eighty-Ninth" -> 89;
            case "Ninetieth" -> 90;
            case "Ninety-First" -> 91;
            case "Ninety-Second" -> 92;
            case "Ninety-Third" -> 93;
            case "Ninety-Fourth" -> 94;
            case "Ninety-Fifth" -> 95;
            case "Ninety-Sixth" -> 96;
            case "Ninety-Seventh" -> 97;
            case "Ninety-Eighth" -> 98;
            case "Ninety-Ninth" -> 99;
            case "Hundredth" -> 100;
            default -> null;
        };
    }
}
