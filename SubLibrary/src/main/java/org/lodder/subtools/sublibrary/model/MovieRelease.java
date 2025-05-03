package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;

import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.Nullable;

public final class MovieRelease extends Release {

    @var @Nullable Integer year;

    public MovieRelease(String name, @Nullable Path file=null, @Nullable String releaseGroup=null,
        @Nullable String quality=null, @Nullable String extension=null, @Nullable Integer year=null) {
        super(name, VideoType.MOVIE, file, releaseGroup, quality, extension);
        this.year = year;
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
