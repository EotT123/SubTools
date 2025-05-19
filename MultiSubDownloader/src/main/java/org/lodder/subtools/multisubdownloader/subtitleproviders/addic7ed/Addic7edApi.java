package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed;

import static java.nio.charset.StandardCharsets.*;
import static manifold.science.util.UnitConstants.*;
import static org.lodder.subtools.sublibrary.util.Sleep.*;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extensions.java.lang.String.StringExt;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.science.measures.Time;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception.Addic7edException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.model.Addic7edMovieSubtitleId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.model.Addic7edSubtitle;
import org.lodder.subtools.sublibrary.Credentials;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.ManagerException;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.control.ReleaseParser.ReleaseParserExtraInfo;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class Addic7edApi implements SubtitleApi {

    private static final Time RATE_DURATION = 1 s; // seconds

    private static final String DOMAIN = "https://www.addic7ed.com";
    private static final Pattern MOVIE_NAME_PATTERN = Pattern.compile("(?<title>.*?) \\((?<year>\\d{4})\\)");
    private static final Pattern TITLE_PATTERN = Pattern.compile(".*? - \\d+x\\d+ - (.*)");
    private static final Pattern VERSION_PATTERN = Pattern.compile("Version (?<info>.+), Duration: \\d+\\.\\d+");
    @val @override Manager manager;
    @val @override SubtitleSource source = SubtitleSource.ADDIC7ED;
    private final boolean speedy;
    private Time lastRequest = Time.now();

    public Addic7edApi(Manager manager, boolean speedy, Credentials credentials=null) throws Addic7edException {
//        super(manager, "Mozilla/5.25 Netscape/5.0 (Windows; I; Win95)");
        this.manager = manager;
        this.speedy = speedy;
        if (credentials != null) {
            login(credentials);
        }
    }

    public void login(Credentials credentials) throws Addic7edException {
        try {
            manager.postBuilder("$DOMAIN/dologin.php")
                .addData("username", credentials.username)
                .addData("password", credentials.password)
                .addData("remember", "false")
                .post();
        } catch (ManagerException e) {
            throw new Addic7edException(e);
        }
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    public List<Addic7edMovieSubtitleId> getMovieProviderIds(String title,
        @Nullable Integer year=null) throws Addic7edException {
        return getCache("providerId", b -> b.add("title", title))
            .getCollection(() -> {
                try {
                    return getContent("$DOMAIN/search.php?Submit=Search&search=" + title.urlEncode()).selectAllByCss(
                            "form[action='/search.php'] ~ table td a").stream()
                        .map(elem -> {
                            String text = elem.text();
                            String providerId = elem.attr("href");
                            String _title = null;
                            Integer _year = null;
                            Matcher matcher = MOVIE_NAME_PATTERN.matcher(text);
                            if (matcher.matches()) {
                                _title = matcher.group("title");
                                _year = Integer.parseInt(matcher.group("year"));
                            }
                            return new Addic7edMovieSubtitleId(text, providerId, _title, _year);
                        }).toList();
                } catch (Exception e) {
                    throw new Addic7edException(e);
                }
            });
    }

    public List<Addic7edSubtitle> searchMovieSubtitles(String providerId, Language language) throws Addic7edException {
        return searchSubtitles(providerId, "$DOMAIN/$providerId", language);
    }

    // ===== \\
    // SERIE \\
    // ===== \\


    public List<ProviderId> getSerieProviderId(String serieName) throws Addic7edException {
        if (StringUtils.isBlank(serieName)) {
            return List.of();
        }
        return getCache("providerId", b -> b.add("serieName", serieName)).getCollection(() -> {
            try {
                List<ProviderId> providerIds =
                    getContent("$DOMAIN/allshows/" + serieName.urlEncode()).select("#container a[href^='/show/']")
                    .stream().map(elem -> new ProviderId(elem.text(), elem.attr("href").split("/")[2]))
                    .toList();
                String serieNameFormatted = serieName.keepLettersOnly().toLowerCase();
                List<ProviderId> providerIdsFormatted = providerIds.stream().filter(providerId -> {
                    String formattedSerieName = providerId.name.keepLettersOnly().toLowerCase();
                    return serieNameFormatted.contains(formattedSerieName) ||
                        formattedSerieName.contains(serieNameFormatted);
                }).toList();
                return !providerIdsFormatted.isEmpty() ? providerIdsFormatted : providerIds;
            } catch (Exception e) {
                throw new Addic7edException(e);
            }
        });
    }

    public List<Addic7edSubtitle> searchSerieSubtitles(String providerId, String providerName, int season, int episode,
        Language language) throws Addic7edException {

        return Addic7edLanguage.of(language)
            .map(lang -> "%s/serie/%s/%s/%s/%s".formatted(DOMAIN,
                URLEncoder.encode(providerName.replace(" ", "_"), UTF_8), season, episode, lang.id))
            .flatMapEx(url -> searchSubtitles(providerId, url, language).stream())
            .toList();
    }

    // ====== \\
    // COMMON \\
    // ====== \\

    private List<Addic7edSubtitle> searchSubtitles(String providerId, String url, Language language)
        throws Addic7edException {
        return getCache("subtitles",
            b -> b.add("providerId", providerId).add("url", url).add("language", language))
            .getCollection(() -> {
                Document doc = getContent(url);
                String title = null;

                Elements elTitle = doc.getElementsByClass("titulo");
                if (elTitle.size() == 1) {
                    Matcher matcher = TITLE_PATTERN.matcher(elTitle.first.html());
                    if (matcher.matches()) {
                        title = StringUtils.trimToNull(matcher.group(1));
                    }
                }

                Elements blocks = doc.select(".tabel95[width='100%']");

                List<Addic7edSubtitle> lSubtitles = new ArrayList<>();
                for (Element block : blocks) {
                    String uploader = "";
                    String version = null;
                    Addic7edLanguage lang = null;
                    String download = null;
                    boolean hearingImpaired = false;

                    Elements classesNewsTitle = block.getElementsByClass("NewsTitle");
                    Elements classesNewsDate = block.getElementsByClass("newsDate").select("td[colspan=3]");
                    if (classesNewsTitle.size() == 1 && classesNewsDate.size() == 1) {
                        Matcher m = VERSION_PATTERN.matcher(classesNewsTitle.first.text().trim());
                        if (!m.matches()) {
                            break;
                        } else {
                            version = StringUtils.trimToNull(m.group("info").trim());
                            uploader = block.selectFirst("a[href*=user/]").text();
                            hearingImpaired = !block.select("img[title~=Hearing]").isEmpty();
                        }
                    }

                    if (version != null) {
                        Elements tds = block.select("tr:contains(Completed)");
                        Elements reqTds = tds.select("td").not("td[rowspan=2]");
                        for (Element td : reqTds) {
                            if (td.hasClass("language")) {
                                lang = Addic7edLanguage.of(td.html().substring(0, td.html().indexOf("<")));
                            }

                            // incomplete not wanted
                            if ((lang != null && td.toString().toLowerCase().contains("completed")) &&
                                td.html().toLowerCase().contains("% completed")) {
                                lang = null;
                            }

                            Elements downloadElements = td.getElementsByClass("buttonDownload");
                            if (lang != null && !downloadElements.isEmpty()) {
                                if (downloadElements.size() == 1) {
                                    download = DOMAIN + downloadElements.first.attr("href");
                                } else if (downloadElements.size() == 2) {
                                    download = DOMAIN + downloadElements.get(1).attr("href");
                                }
                            }
                            if (lang != null && download != null && title != null) {
                                ReleaseParserExtraInfo extraInfoParser = ReleaseParser.parseExtraInfo(version);
                                Addic7edSubtitle sub = new Addic7edSubtitle(
                                    url:download,
                                    fileName:StringExt.removeIllegalFilenameChars(title + " " + version),
                                    language:lang.language,
                                    quality:extraInfoParser.getQualityKeyword(),
                                    releaseGroup:extraInfoParser.getReleaseGroupBestEffort(),
                                    uploader:uploader,
                                    hearingImpaired:hearingImpaired,
                                    version:version);
                                if (language == sub.language && !isDuplicate(lSubtitles, sub)) {
                                    lSubtitles.add(sub);
                                }
                                lang = null;
                                download = null;
                            }
                        }
                    }
                }
                return lSubtitles;
            });
    }

    public boolean isDuplicate(List<Addic7edSubtitle> lSubtitles, Addic7edSubtitle sub) {
        return lSubtitles.stream().anyMatch(s -> s.language == sub.language && StringUtils.equals(s.url, sub.url) &&
            StringUtils.equals(s.version, sub.version));
    }

    private Document getContent(String url) throws Addic7edException {
        try {
            if (!speedy && getCache("content", b -> b.add("url", url)).isNotPresent()) {
                // if (ChronoUnit.SECONDS.between(lastRequest, LocalDateTime.now()) < RATEDURATION) {
                // LOGGER.info("RateLimit is reached for ADDIC7ed, please wait {} seconds", RATEDURATION);
                // }
                Time timeToSleep = RATE_DURATION - Time.now() + lastRequest;
                if (timeToSleep.isPositive) {
                    sleep(timeToSleep);
                }
                lastRequest = Time.now();
            }
            return manager.getAsJsoupDocument(PageContentParams.params(url:url, userAgent:""));
        } catch (Exception e) {
            throw new Addic7edException(e);
        }
    }
}
