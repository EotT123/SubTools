package org.lodder.subtools.sublibrary.data.imdb;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.imdb.exception.ImdbException;
import org.lodder.subtools.sublibrary.data.imdb.model.ImdbDetails;

@RequiredArgsConstructor
public class ImdbApi {

    private static final String DOMAIN = "https://www.imdb.com";
    private final Manager manager;

    public Optional<ImdbDetails> getMovieDetails(int imdbId) throws ImdbException {
        return manager.valueBuilder()
                .memoryCache()
                .key("IMDB-moviedetails-$imdbId")
                .optionalSupplier(() -> {
                    final String url = "$DOMAIN/title/tt${String.format(\"%07d\", imdbId)}/releaseinfo";
                    try {
                        org.jsoup.nodes.Element element = manager.getPageContentBuilder()
                                .url(url)
                                .getAsJsoupDocument()
                                .selectFirst(".article .subpage_title_block .subpage_title_block__right-column");
                        String imdbName = element.selectFirst("a[itemprop='url']").text();
                        int year = Integer.parseInt(element.selectFirst("span.nobr").text().replaceAll("[^0-9]", ""));
                        return Optional.of(new ImdbDetails(imdbName, year));
                    } catch (Exception e) {
                        throw new ImdbException("Error IMDBAPI", url, e);
                    }
                }).getOptional();
    }
}
