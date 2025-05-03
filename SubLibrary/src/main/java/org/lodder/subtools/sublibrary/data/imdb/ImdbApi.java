package org.lodder.subtools.sublibrary.data.imdb;

import static org.lodder.subtools.sublibrary.PageContentParams.*;

import java.util.Optional;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.data.imdb.exception.ImdbException;
import org.lodder.subtools.sublibrary.data.imdb.model.ImdbDetails;

public class ImdbApi implements ApiIntf {

    private static final String DOMAIN = "https://www.imdb.com";
    @val @override Manager manager;
    @val @override String provider = "IMDB";

    public ImdbApi(Manager manager) {
        this.manager = manager;
    }

    // TODO use IMDB API
    public Optional<ImdbDetails> getMovieDetails(String imdbId) throws ImdbException {
        return getCache("moviedetails", b -> b.add("imdbId", imdbId))
            .getOptional(() -> {
                final String url = "$DOMAIN/title/$imdbId/releaseinfo";
                try {
                    org.jsoup.nodes.Element element = manager.getAsJsoupDocument(url(url))
                        .selectFirstByCss(".article .subpage_title_block .subpage_title_block__right-column");
                    String imdbName = element.selectFirstByCss("a[itemprop='url']").text();
                    int year = Integer.parseInt(
                        element.selectFirstByCss("span.nobr").text().replaceAll("[^0-9]", ""));
                    return Optional.of(new ImdbDetails(imdbName, year));
                } catch (Exception e) {
                    throw new ImdbException("Error $provider API", url, e);
                }
            });
    }
}
