package org.lodder.subtools.sublibrary.data.omdb;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.omdb.exception.OmdbException;
import org.lodder.subtools.sublibrary.data.omdb.model.OmdbDetails;
import org.w3c.dom.Node;

@RequiredArgsConstructor
class OmdbApi {

    private static final String DOMAIN = "http://www.omdbapi.com";
    private final Manager manager;

    public Optional<OmdbDetails> getMovieDetails(int imdbId) throws OmdbException {
        return manager.valueBuilder()
                .memoryCache()
                .key("OMDB-moviedetails-$imdbId")
                .optionalSupplier(() -> {
                    final String url = "$DOMAIN/?i=tt$%07d&plot=short&r=xml".formatted(imdbId);
                    try {
                        return manager.getPageContentBuilder()
                                .url(url)
                                .getAsDocument()
                            .getElementsByTagName("movie").stream()
                                .map(this::parseOMDBDetails).findFirst();
                    } catch (Exception e) {
                        throw new OmdbException("Error OMDB API", url, e);
                    }
                }).getOptional();
    }

    private OmdbDetails parseOMDBDetails(Node node) {
        return new OmdbDetails(node.getAttribute("title"), Integer.parseInt(node.getAttribute("year")));
    }

}
