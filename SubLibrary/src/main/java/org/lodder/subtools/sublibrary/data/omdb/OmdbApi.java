package org.lodder.subtools.sublibrary.data.omdb;

import static org.lodder.subtools.sublibrary.data.omdb.OmdbApi.ParamType.*;
import static org.lodder.subtools.sublibrary.data.omdb.OmdbApi.SearchParamByIdTitle.*;
import static org.lodder.subtools.sublibrary.data.omdb.OmdbApi.SearchParamCommon.*;

import java.util.Optional;
import java.util.function.Consumer;

import Omdb.Release;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.json.rt.api.Requester;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.data.omdb.exception.OmdbApiException;

class OmdbApi implements ApiIntf {

    private static final String API_KEY = "74473b06";
    private static final String API_DOMAIN = "http://www.omdbapi.com";
    @val @override Manager manager;
    @val @override String provider = "OMDB";

    public OmdbApi(Manager manager) {
        this.manager = manager;
    }

    public Optional<Release> searchRelease(String imdbId) throws OmdbApiException {
        return getCache("release", b -> b.add("imdbId", imdbId))
            .getOptional(() -> search(req -> req.withParam(IMDB_ID, imdbId)));
    }

    public Optional<Release> searchMovie(String title, Integer year=null) throws OmdbApiException {
        return getCache("movie", b -> b.add("title", title).add("year", year))
            .getOptional(
                () -> search(req -> req.withParam(TITLE, title.replace(" ", "+")).withParam(RESULT_TYPE, MOVIE)));
    }

    public Optional<Release> searchSerie(String name) throws OmdbApiException {
        return getCache("serie", b -> b.add("name", name))
            .getOptional(() -> search(req -> req.withParam(TITLE, name.replace(" ", "+")).withParam(RESULT_TYPE,
                SERIE)));
    }

    private static Optional<Release> search(Consumer<RequesterWrapper<Release>> extraParamConsumer)
        throws OmdbApiException {
        try {
            Requester<Release> request = Release.request(API_DOMAIN).withParam("apikey", API_KEY);
            extraParamConsumer.accept(new RequesterWrapper<>(request));
            Release release = request.getOne();
            return release.response ? Optional.of(release) : Optional.empty();
        } catch (Exception e) {
            throw OmdbApiException.error(null, e.getMessage().replace("apikey=" + API_KEY, ""));
        }
    }

    /*----------------*/
    /* API PARAMETERS */
    /*----------------*/

    private static class RequesterWrapper<T> {

        @val Requester<T> requester;

        public RequesterWrapper(Requester<T> requester) {
            this.requester = requester;
        }

        RequesterWrapper<T> withParam(SearchParamIntf param, String value) {
            requester.withParam(param.value, value);
            return this;
        }

        RequesterWrapper<T> withParam(SearchParamIntf param, ParamIntf value) {
            return withParam(param, value.value);
        }
    }

    private interface SearchParamIntf {
        @val String value;
    }

    private interface ParamIntf {
        @val String value;
    }

    enum SearchParamCommon implements SearchParamIntf {
        TITLE("t"),
        RESULT_TYPE("type"),
        YEAR("y"),
        DATA_RETURN_TYPE("r"),
        CALLBACK("callback"),
        API_VERSION("v");

        @override @val String value;

        SearchParamCommon(String value) {
            this.value = value;
        }
    }

    enum SearchParamByIdTitle implements SearchParamIntf {
        IMDB_ID("i"),
        PLOT("plot");

        @override @val String value;

        SearchParamByIdTitle(String value) {
            this.value = value;
        }
    }

    enum SearchParamBySearch implements SearchParamIntf {
        PAGE("page");

        @override @val String value;

        SearchParamBySearch(String value) {
            this.value = value;
        }
    }

    enum ParamType implements ParamIntf {
        MOVIE("movie"), SERIE("series"), EPISODE("episode");

        @override @val String value;

        ParamType(String value) {
            this.value = value;
        }
    }

    enum ParamPlot implements ParamIntf {
        SHORT("short"), FULL("full");

        @override @val String value;

        ParamPlot(String value) {
            this.value = value;
        }
    }

    enum ParamDataReturnType implements ParamIntf {
        JSON("json"), XML("xml");

        @override @val String value;

        ParamDataReturnType(String value) {
            this.value = value;
        }
    }
}
