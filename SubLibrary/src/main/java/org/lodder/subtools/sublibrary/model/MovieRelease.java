package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;
import java.util.OptionalInt;

import manifold.ext.props.rt.api.var;

public final class MovieRelease extends Release {

    @var String name;
    @var Integer year;
    private int imdbId;
    private int tvdbId;

    public MovieRelease(String name, Path file=null, String releaseGroup=null, String quality=null,
        String extension=null, Integer year=null) {
        super(VideoType.MOVIE, file, releaseGroup, quality, extension);
        this.name = name;
        this.year = year;
    }

    public String getImdbIdAsString() {
        return "tt%07d".formatted(imdbId);
    }

    public OptionalInt getTvdbId() {
        return tvdbId == 0 ? OptionalInt.empty() : OptionalInt.of(tvdbId);
    }

    public void setTvdbId(int tvdbId) {
        this.tvdbId = tvdbId;
    }

    public OptionalInt getImdbId() {
        return imdbId == 0 ? OptionalInt.empty() : OptionalInt.of(imdbId);
    }

    public void setImdbId(int imdbId) {
        this.imdbId = imdbId;
    }

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $name ${quality} ${releaseGroup}";
    }

    @Override
    public String getReleaseDescription() {
        return name;
    }
}
