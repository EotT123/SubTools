package org.lodder.subtools.sublibrary.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class SeasonEpisode {

    private static final Pattern SEASON_EPISODE_PATTERN_1 = Pattern.compile("S(\\d{1,2})E(\\d{1,2})");
    private static final Pattern SEASON_EPISODE_PATTERN_2 = Pattern.compile("[. ](\\d{1,2})x(\\d{1,2})");
    private static final Pattern SEASON_EPISODES_PATTERN_1 = Pattern.compile("S(\\d{1,2})E(\\d{1,2})E(\\d{1,2})");
    @val int season;
    @val int[] episodes;

    public SeasonEpisode(int season, int... episodes) {
        this.season = season;
        this.episodes = episodes;
    }

    public static @Nullable SeasonEpisode fromText(String text) {
        Matcher matcher = SEASON_EPISODES_PATTERN_1.matcher(text);
        if (matcher.find()) {
            return new SeasonEpisode(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)));
        }
        Matcher matcher2 = SEASON_EPISODE_PATTERN_1.matcher(text);
        if (matcher2.find()) {
            return new SeasonEpisode(Integer.parseInt(matcher2.group(1)), Integer.parseInt(matcher2.group(2)));
        }
        Matcher matcher3 = SEASON_EPISODE_PATTERN_2.matcher(text);
        if (matcher3.find()) {
            return new SeasonEpisode(Integer.parseInt(matcher3.group(1)), Integer.parseInt(matcher3.group(2)));
        }
        return null;
    }

    public boolean containsEpisode(int episode) {
        return episodes.contains(episode);
    }
}
