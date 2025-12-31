package org.lodder.subtools.sublibrary.model;

import java.util.Set;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.imdb.model.ImdbDetails;

@NullMarked
public sealed class TvReleaseWithoutPath extends ReleaseWithoutPath implements TvRelease permits TvReleaseWithPath {

    // parsed from the filename
    @val @override int season;
    @val @override Set<Integer> episodes;
    @var @override @Nullable String title;
    // tvdb name
    @var @override @Nullable String originalName;
    @val @override boolean special;
    // custom name which can be used to search subtitle providers
    @val @override @Nullable String customName;

    public TvReleaseWithoutPath(String name, int season, int episode, @Nullable String releaseGroup=null,
        @Nullable String quality=null, @Nullable String originalName=null, @Nullable String customName=null,
        @Nullable String title=null, boolean special=false, String completeName) {
        this(name, season, Set.of(episode), releaseGroup, quality, originalName, customName, title, special,
            completeName);
    }

    public TvReleaseWithoutPath(String name, int season, Set<Integer> episodes, @Nullable String releaseGroup=null,
        @Nullable String quality=null, @Nullable String originalName=name, @Nullable String customName=null,
        @Nullable String title=null, boolean special=false, String completeName) {
        super(name, releaseGroup, quality, completeName);
        this.title = title;
        this.season = season;
        this.episodes = Set.copyOf(episodes);
        this.special = special;
        this.originalName = originalName;
        this.customName = customName;
    }

    public void updateImdbEpisodeInfo(ImdbDetails tvdbEpisode) {
        // TODO implement this
    }

    @Override
    public String getReleaseDescription() {
        return nameWithSeasonEpisode;
    }

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $name s$season e$episodes $quality $releaseGroup";
    }
}
