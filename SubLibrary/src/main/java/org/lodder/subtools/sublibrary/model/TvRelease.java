package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.apache.commons.lang3.StringUtils;
import org.lodder.subtools.sublibrary.data.tvdb.model.TheTvdbEpisode;

public final class TvRelease extends Release {

    // parsed from the filename
    @val String name;
    @val List<Integer> episodes;
    @val int season;
    @var String title;
    @var int tvdbId;
    // tvdb name
    @var String originalName;
    @val boolean special;
    // custom name which can be used to search subtitle providers
    @val String customName;

    public TvRelease(Path file=null, String releaseGroup=null, String quality=null, String extension=null, String name,
        String originalName=null, String customName=null, String title=null, int season, int episode,
        boolean special=false) {
        this(file, releaseGroup, quality, extension, name, originalName, customName, title, season, List.of(episode),
            special);
    }

    public TvRelease(Path file=null, String releaseGroup=null, String quality=null, String extension=null, String name,
        String originalName=null, String customName=null, String title=null, int season, List<Integer> episodes,
        boolean special=false) {
        super(VideoType.EPISODE, file, releaseGroup, quality, extension);
        this.name = name;
        this.title = title;
        this.season = season;
        this.episodes = Collections.unmodifiableList(episodes);
        this.special = special;
        this.originalName = originalName;
        this.customName = customName;
    }

    public String getNameWithSeasonEpisode() {
        return formatName(name, season, episodes.isEmpty() ? -1 : firstEpisode);
    }

    public static String formatName(String serieName, int season, int episode) {
        return serieName + " " + formatSeasonEpisode(season, episode);
    }

    public static String formatSeasonEpisode(int season, int episode) {
        return "S%02dE%02d".formatted(season, episode);
    }

    public void updateTvdbEpisodeInfo(TheTvdbEpisode tvdbEpisode) {
        this.title = tvdbEpisode.episodeName; // update to reflect correct episode title
    }

    public OptionalInt getTvdbIdOptional() {
        return tvdbId == 0 ? OptionalInt.empty() : OptionalInt.of(tvdbId);
    }

    public int getFirstEpisode() {
        return episodes.first;
    }

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $name s$season e$episodes $quality $releaseGroup";
    }

    @Override
    public String getReleaseDescription() {
        return getNameWithSeasonEpisode();
    }

    public String getDisplayName() {
        return StringUtils.defaultIfBlank(originalName, name);
    }
}
