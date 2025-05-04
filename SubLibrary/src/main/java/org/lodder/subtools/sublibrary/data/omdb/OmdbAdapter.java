package org.lodder.subtools.sublibrary.data.omdb;

import java.util.Optional;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.AdapterIntf;
import org.lodder.subtools.sublibrary.data.omdb.exception.OmdbException;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

public class OmdbAdapter implements AdapterIntf {

    private static OmdbAdapter instance;
    private final OmdbApi api;
    @val @override Manager manager;
    @val @override String provider = "OMDB";

    public OmdbAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        this.manager = manager;
        this.api = new OmdbApi(manager);
    }

    public Optional<Omdb.Release> searchReleaseWithImdbId(String imdbId) {
        try {
            return getCache("release", b -> b.add("imdbId", imdbId))
                .getOptional(
                    supplier:() -> api.searchRelease(imdbId),
                    storeTempNullValue:true);
        } catch (OmdbException e) {
            return Optional.empty();
        }
    }


    public Optional<Omdb.Release> searchMovie(String title, Integer year=null) {
        try {
            return getCache("movie", b -> b.add("title", title).add("year", year))
                .getOptional(
                    supplier:() -> api.searchMovie(title, year),
                    storeTempNullValue:true);
        } catch (OmdbException e) {
            return Optional.empty();
        }
    }

    public Optional<Omdb.Release> searchSerie(String name) {
        try {
            return getCache("serie", b -> b.add("name", name))
                .getOptional(
                    supplier:() -> api.searchSerie(name),
                    storeTempNullValue:true);
        } catch (OmdbException e) {
            return Optional.empty();
        }
    }

    public static synchronized OmdbAdapter getInstance(Manager manager, UserInteractionHandler userInteractionHandler) {
        if (instance == null) {
            instance = new OmdbAdapter(manager, userInteractionHandler);
        }
        return instance;
    }
}
