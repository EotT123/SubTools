package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model;

import static java.util.Objects.*;

import java.util.Objects;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenSubtitlesMovieMetadata {

    @var String name;
    @val int year;
    @val int imdbId;

    public OpenSubtitlesMovieMetadata(String name, int year, int imdbId) {
        this.name = name;
        this.year = year;
        this.imdbId = imdbId;
    }

    public OpenSubtitlesMovieMetadata(String name, int imdbId) {
        this(name, -1, imdbId);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof OpenSubtitlesMovieMetadata that
            && year == that.year
            && imdbId == that.imdbId
            && Objects.equals(name, that.name));
    }

    @Override
    public int hashCode() {
        return hash(name, year, imdbId);
    }

    @Override
    public String toString() {
        if (year < 0) {
            return name;
        }
        return "$name ($year)";
    }
}
