package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class MovieReleaseWithPath extends MovieReleaseWithoutPath implements ReleaseWithPath, MovieRelease {

    @val @override Path path;

    public MovieReleaseWithPath(MovieReleaseWithoutPath movieRelease, Path path) {
        this(movieRelease.name, path, movieRelease.releaseGroup, movieRelease.quality, movieRelease.year);
    }

    public MovieReleaseWithPath(String name, Path file, @Nullable String releaseGroup=null,
        @Nullable String quality=null, @Nullable Integer year=null) {
        super(name, releaseGroup, quality, year, file.fileNameAsString);
        this.path = file;
    }
}
