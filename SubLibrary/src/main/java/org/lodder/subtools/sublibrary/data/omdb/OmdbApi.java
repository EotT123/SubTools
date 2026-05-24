package org.lodder.subtools.sublibrary.data.omdb;

import static org.lodder.subtools.sublibrary.data.omdb.OmdbApi.ParamType.*;
import static org.lodder.subtools.sublibrary.data.omdb.OmdbApi.SearchParamByIdTitle.*;
import static org.lodder.subtools.sublibrary.data.omdb.OmdbApi.SearchParamCommon.*;

import java.util.function.Consumer;

import Omdb.Release;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.json.rt.api.Requester;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.data.omdb.exception.OmdbApiException;

@NullMarked
class OmdbApi implements ApiIntf {

    private static final String API_KEY = "74473b06";
    private static final String API_DOMAIN = "http://www.omdbapi.com";
    @val @override String provider = "OMDB";

    public @Nullable Release searchRelease(String imdbId) throws OmdbApiException {
        return getCache("release", b -> b.add("imdbId", imdbId))
            .get(() -> search(req -> req.withParam(IMDB_ID, imdbId)));
    }

    public @Nullable Release searchMovie(String title, @Nullable Integer year=null) throws OmdbApiException {
        return getCache("movie", b -> b.add("title", title).add("year", year))
            .get(() -> search(req -> req.withParam(TITLE, title.replace(" ", "+")).withParam(RESULT_TYPE, MOVIE)));
    }

    public @Nullable Release searchSerie(String name) throws OmdbApiException {
        return getCache("serie", b -> b.add("name", name))
            .get(() -> search(req -> req.withParam(TITLE, name.replace(" ", "+")).withParam(RESULT_TYPE, SERIE)));
    }

    private static @Nullable Release search(Consumer<RequesterWrapper<Release>> extraParamConsumer)
        throws OmdbApiException {
        try {
            Requester<Release> request = Release.request(API_DOMAIN).withParam("apikey", API_KEY);
            extraParamConsumer.accept(new RequesterWrapper<>(request));
            Release release = request.getOne();
            return release.response ? release : null;
        } catch (Exception e) {
            throw OmdbApiException.error(null, e.getMessage().replace("apikey=" + API_KEY, ""));
        }
    }

    /*----------------*/
    /* API PARAMETERS */
    /*----------------*/

    @NullMarked
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

    @NullMarked
    private interface SearchParamIntf {
        @val String value;
    }

    @NullMarked
    private interface ParamIntf {
        @val String value;
    }

    @NullMarked
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

    @NullMarked
    enum SearchParamByIdTitle implements SearchParamIntf {
        IMDB_ID("i"),
        PLOT("plot");

        @override @val String value;

        SearchParamByIdTitle(String value) {
            this.value = value;
        }
    }

    @NullMarked
    enum SearchParamBySearch implements SearchParamIntf {
        PAGE("page");

        @override @val String value;

        SearchParamBySearch(String value) {
            this.value = value;
        }
    }

    @NullMarked
    enum ParamType implements ParamIntf {
        MOVIE("movie"),
        SERIE("series"),
        EPISODE("episode");

        @override @val String value;

        ParamType(String value) {
            this.value = value;
        }
    }

    @NullMarked
    enum ParamPlot implements ParamIntf {
        SHORT("short"),
        FULL("full");

        @override @val String value;

        ParamPlot(String value) {
            this.value = value;
        }
    }

    @NullMarked
    enum ParamDataReturnType implements ParamIntf {
        JSON("json"),
        XML("xml");

        @override @val String value;

        ParamDataReturnType(String value) {
            this.value = value;
        }
    }
}
