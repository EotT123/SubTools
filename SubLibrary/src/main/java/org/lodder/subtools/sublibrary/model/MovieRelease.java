package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;
import java.util.OptionalInt;

import lombok.Setter;
import lombok.experimental.Accessors;
import manifold.ext.props.rt.api.set;
import manifold.ext.props.rt.api.var;

public final class MovieRelease extends Release {

    @var String name;
    @var Integer year;
    @set int imdbId;
    @set int tvdbId;

    public interface MovieReleaseBuilderName {
        MovieReleaseBuilderOther name(String name);
    }

    public interface MovieReleaseBuilderOther {
        MovieReleaseBuilderOther file(Path file);

        MovieReleaseBuilderOther quality(String quality);

        MovieReleaseBuilderOther description(String description);

        MovieReleaseBuilderOther releaseGroup(String releaseGroup);

        MovieReleaseBuilderOther year(Integer year);

        MovieRelease build();
    }

    public static MovieReleaseBuilderName builder() {
        return new MovieReleaseBuilder();
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    public static class MovieReleaseBuilder implements MovieReleaseBuilderOther, MovieReleaseBuilderName {
        private String name;
        private Integer year;

        private String quality;
        private Path file;
        private String description;
        private String releaseGroup;

        @Override
        public MovieRelease build() {
            return new MovieRelease(file, description, releaseGroup, quality, name, year == null ? 0 : year);
        }
    }

    private MovieRelease(Path file, String description, String releaseGroup, String quality, String name, int year) {
        super(VideoType.MOVIE, file, description, releaseGroup, quality);
        this.name = name;
        this.year = year;
    }

    public String getImdbIdAsString() {
        return "tt" + String.format("%07d", imdbId);
    }

    public OptionalInt getTvdbId() {
        return tvdbId == 0 ? OptionalInt.empty() : OptionalInt.of(tvdbId);
    }

    public OptionalInt getImdbId() {
        return imdbId == 0 ? OptionalInt.empty() : OptionalInt.of(imdbId);
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
