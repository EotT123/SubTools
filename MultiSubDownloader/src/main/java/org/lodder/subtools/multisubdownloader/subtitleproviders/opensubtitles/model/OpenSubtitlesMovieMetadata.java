package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model;

import java.util.Objects;

import lombok.AllArgsConstructor;
import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;

@AllArgsConstructor
public class OpenSubtitlesMovieMetadata {

    @var String name;
    @val int year;
    @val int imdbId;

    public OpenSubtitlesMovieMetadata(String name, int imdbId) {
        this(name, -1, imdbId);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof OpenSubtitlesMovieMetadata other
                && imdbId == other.imdbId && year == other.year && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, year, imdbId);
    }

    @Override
    public String toString() {
        if (year < 0) {
            return name;
        }
        return "$name ($year)";
    }
}
