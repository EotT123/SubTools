package org.lodder.subtools.sublibrary.data.omdb;

import java.util.Optional;
import java.util.function.Consumer;

import Omdb.Release;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.json.rt.api.Requester;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.data.omdb.exception.OmdbException;

class OmdbApi implements ApiIntf {

    private static final String API_KEY = "74473b06";
    private static final String API_DOMAIN = "http://www.omdbapi.com";
    @val @override Manager manager;
    @val @override String provider = "OMDB";

    public OmdbApi(Manager manager) {
        this.manager = manager;
    }

    public Optional<Release> searchRelease(String imdbId) throws OmdbException {
        return getCache("release", b -> b.add("imdbId", imdbId))
            .getOptional(() -> search(req -> req.withParam("i", imdbId)));
    }

    public Optional<Release> searchMovie(String title, Integer year=null) throws OmdbException {
        return getCache("movie", b -> b.add("title", title).add("year", year))
            .getOptional(() -> search(req -> req.withParam("t", title).withParam("type", "movie")));
    }

    public Optional<Release> searchSerie(String name) throws OmdbException {
        return getCache("serie", b -> b.add("name", name))
            .getOptional(() -> search(req -> req.withParam("t", name).withParam("type", "series")));
    }

    private static Optional<Release> search(Consumer<Requester<Release>> extraParamConsumer) {
        try {
            Requester<Release> request = Release.request(API_DOMAIN).withParam("apikey", API_KEY);
            extraParamConsumer.accept(request);
            Release release = request.getOne();
            return release.response ? Optional.of(release) : Optional.empty();
        } catch (Exception e) {
            throw new OmdbException("Error OMDB API", e);
        }
    }
}
