package org.lodder.subtools.sublibrary.model;

import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public sealed interface MovieRelease extends Release permits MovieReleaseWithPath, MovieReleaseWithoutPath {

    @var @Nullable Integer year;

    @Override
    default boolean isOfType(VideoType videoType) {
        return videoType == VideoType.MOVIE;
    }
}
