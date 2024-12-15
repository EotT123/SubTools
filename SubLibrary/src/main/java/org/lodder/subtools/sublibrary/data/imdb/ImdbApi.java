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
                    final String url = "$DOMAIN/title/tt${%07d/releaseinfo".formatted(imdbId);
                    try {
                        org.jsoup.nodes.Element element = manager.getPageContentBuilder()
                                .url(url)
                                .getAsJsoupDocument()
                                .selectFirst(".article .subpage_title_block .subpage_title_block__right-column");
                        String imdbName = element.getFirstElementByCss("a[itemprop='url']").getText();
                        int year = Integer.parseInt(
                                element.getFirstElementByCss("span.nobr").getText().replaceAll("[^0-9]", ""));
                        return Optional.of(new ImdbDetails(imdbName, year));
                    } catch (Exception e) {
                        throw new ImdbException("Error IMDB API", url, e);
                    }
                }).getOptional();
    }
}
