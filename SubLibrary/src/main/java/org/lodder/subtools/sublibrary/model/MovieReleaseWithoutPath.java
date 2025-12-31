package org.lodder.subtools.sublibrary.model;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public sealed class MovieReleaseWithoutPath extends ReleaseWithoutPath implements MovieRelease
    permits MovieReleaseWithPath {

    @var @override @Nullable Integer year;

    public MovieReleaseWithoutPath(String name, @Nullable String releaseGroup=null,
        @Nullable String quality=null, @Nullable Integer year=null, String completeName) {
        super(name, VideoType.MOVIE, releaseGroup, quality, completeName);
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
