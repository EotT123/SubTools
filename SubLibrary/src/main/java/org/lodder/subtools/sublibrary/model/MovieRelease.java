package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;
import java.util.OptionalInt;

import lombok.Setter;
import lombok.experimental.Accessors;
import manifold.ext.props.rt.api.var;

public final class MovieRelease extends Release {

    @var String name;
    @var Integer year;
    private int imdbId;
    private int tvdbId;

    public interface MovieReleaseBuilderName {
        MovieReleaseBuilderOther name(String name);
    }

    public interface MovieReleaseBuilderOther {
        MovieReleaseBuilderOther file(Path file);

        MovieReleaseBuilderOther quality(String quality);

        MovieReleaseBuilderOther releaseGroup(String releaseGroup);

        MovieReleaseBuilderOther year(Integer year);
        
        MovieReleaseBuilderOther extension(String extension);

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
        private String releaseGroup;
        private String extension;

        @Override
        public MovieRelease build() {
            return new MovieRelease(file, releaseGroup, quality, extension, name, year == null ? 0 : year);
        }
    }

    private MovieRelease(Path file, String releaseGroup, String quality, String extension, String name, int year) {
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
