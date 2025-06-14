package org.lodder.subtools.sublibrary.data.imdb;

import static org.lodder.subtools.sublibrary.PageContentParams.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKeyBuilder;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.data.imdb.exception.ImdbSearchIdException;
import org.lodder.subtools.sublibrary.data.imdb.model.ImdbId;
import util.Utils;

record ImdbSearchIdApi(Manager manager) {

    private static final Pattern IMDB_URL_ID_PATTERN = Pattern.compile("/title/tt(\\d*)");

    public Set<ImdbId> getImdbIdOnImdb(String title, @Nullable Integer year) throws ImdbSearchIdException {
        return manager.getCache(CacheType.MEMORY,
                new CacheKeyBuilder("IMDB", "imdbid-imdb").add("title", title).add("year", year))
            .getCollection(() -> {
                StringBuilder sb = new StringBuilder("https://www.imdb.com/find/?q=");
                sb.append(URLEncoder.encode(title, StandardCharsets.UTF_8));
                if (year != null) {
                    sb.append("+%28").append(year).append("%29");
                }
                String url = sb.toString().replace("+", "%20");
                try {
                    Elements searchResults = manager.getAsJsoupDocument(url(url)).select(".find-result-item");
                    return getImdbIdCommon(searchResults,
                        e -> e.selectFirst("a").text(),
                        e -> e.selectFirst("a").attr("href"),
                        e -> e.selectFirst("span").text());
                } catch (Exception e) {
                    throw new ImdbSearchIdException("Error getImdbIdOnImdb", url, e);
                }
            });
    }

    public Set<ImdbId> getImdbIdOnYahoo(String title, @Nullable Integer year) throws ImdbSearchIdException {
        return manager.getCache(CacheType.MEMORY,
                new CacheKeyBuilder("IMDB", "imdbid-yahoo").add("title", title).add("year", year))
            .getCollection(() -> {
                StringBuilder sb =
                    new StringBuilder("http://search.yahoo.com/search;_ylt=A1f4cfvx9C1I1qQAACVjAQx.?p=");
                sb.append(URLEncoder.encode(title, StandardCharsets.UTF_8));
                if (year != null) {
                    sb.append("+%28").append(year).append("%29");
                }

                sb.append("+site%3Aimdb.com&fr=yfp-t-501&ei=UTF-8&rd=r1");
                String url = sb.toString();

                try {
                    Elements searchResults = manager.getAsJsoupDocument(url(url))
                        .select("a[href~='https%3a%2f%2fwww.imdb.com%2ftitle%2ftt']");
                    Function<Element, String> toStringMapper = e -> Optional.ofNullable((Element) e.selectFirst("h3"))
                        .map(e2 -> e2.text().replace(" - IMDb", ""))
                        .orElse(null);
                    Function<Element, String> toHrefMapper =
                        e -> URLDecoder.decode(e.attr("href"), StandardCharsets.UTF_8);
                    return getImdbIdCommon(searchResults, toStringMapper, toHrefMapper);
                } catch (Exception e) {
                    throw new ImdbSearchIdException("Error getImdbIdOnYahoo", url, e);
                }
            });
    }

    public Set<ImdbId> getImdbIdOnGoogle(String title, @Nullable Integer year) throws ImdbSearchIdException {
        return manager.getCache(CacheType.MEMORY,
                new CacheKeyBuilder("IMDB", "imdbid-google").add("title", title).add("year", year))
            .getCollection(() -> {
                StringBuilder sb = new StringBuilder("http://www.google.com/search?q=");
                sb.append(URLEncoder.encode(title, StandardCharsets.UTF_8));
                if (year != null) {
                    sb.append("+%28").append(year).append("%29");
                }
                sb.append("+site%3Awww.imdb.com&meta=");
                String url = sb.toString();
                try {
                    Elements searchResults =
                        manager.getAsJsoupDocument(url(url)).select("a[href*='https://www.imdb.com/title/tt']");
                    Function<Element, String> toStringMapper =
                        e -> e.selectFirst("span").text().replace(" - IMDb", "");
                    Function<Element, String> toHrefMapper = e -> e.attr("href");
                    return getImdbIdCommon(searchResults, toStringMapper, toHrefMapper);
                } catch (Exception e) {
                    throw new ImdbSearchIdException("Error getImdbIdOnGoogle", url, e);
                }
            });
    }

    private Set<ImdbId> getImdbIdCommon(Elements searchResults, Function<Element, String> toStringMapper,
        Function<Element, String> toHrefMapper, Function<Element, String> toYearMapper=e -> null) {
        return searchResults.stream().collect(Utils.setCollector(
            (set, element) -> {
                String name = toStringMapper.apply(element);
                if (StringUtils.isBlank(name)) {
                    return;
                }
                String href = toHrefMapper.apply(element);
                Matcher matcher = IMDB_URL_ID_PATTERN.matcher(href);
                if (matcher.find()) {
                    set.add(new ImdbId(name, matcher.group().replace("/title/tt", "tt"), toYearMapper.apply(element)));
                }
            }));
    }
}
