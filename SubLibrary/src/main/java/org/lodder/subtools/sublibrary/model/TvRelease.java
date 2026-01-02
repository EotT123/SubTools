package org.lodder.subtools.sublibrary.model;

import java.util.Set;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public sealed interface TvRelease extends Release permits TvReleaseWithPath, TvReleaseWithoutPath {

    // parsed from the filename
    @val int season;
    @val Set<Integer> episodes;
    @var @Nullable String title;
    // tvdb name
    @var @Nullable String originalName;
    @val boolean special;
    // custom name which can be used to search subtitle providers
    @val @Nullable String customName;

    @val int firstEpisode = episodes.first();
    @val String nameWithSeasonEpisode = formatName(name, season, episodes.isEmpty() ? -1 : firstEpisode);

    static String formatName(String serieName, int season, int episode) {
        return serieName + " " + formatSeasonEpisode(season, episode);
    }

    static String formatSeasonEpisode(int season, int episode) {
        return "S%02dE%02d".formatted(season, episode);
    }

    @val String displayName = StringUtils.defaultIfBlank(originalName, name);

    @Override
    default boolean isOfType(VideoType videoType) {
        return videoType == VideoType.EPISODE;
    }
}
