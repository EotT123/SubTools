package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;
import java.util.Set;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class TvReleaseWithPath extends TvReleaseWithoutPath implements ReleaseWithPath, TvRelease {

    @val @override Path path;

    public TvReleaseWithPath(TvReleaseWithoutPath tvRelease, Path path) {
        this(tvRelease.name, tvRelease.season, tvRelease.episodes, path, tvRelease.releaseGroup, tvRelease.quality,
            tvRelease.originalName, tvRelease.customName, tvRelease.title, tvRelease.special);
    }

    public TvReleaseWithPath(String name, int season, int episode, Path file, @Nullable String releaseGroup=null,
        @Nullable String quality=null, @Nullable String originalName=null, @Nullable String customName=null,
        @Nullable String title=null, boolean special=false) {
        super(name, season, episode, releaseGroup, quality, originalName, customName, title, special,
            file.fileNameAsString);
        this.path = file;
    }

    public TvReleaseWithPath(String name, int season, Set<Integer> episodes, Path file,
        @Nullable String releaseGroup=null, @Nullable String quality=null, @Nullable String originalName=name,
        @Nullable String customName=null, @Nullable String title=null, boolean special=false) {
        super(name, season, episodes, releaseGroup, quality, originalName, customName, title, special,
            file.fileNameAsString);
        this.path = file;
    }
}
