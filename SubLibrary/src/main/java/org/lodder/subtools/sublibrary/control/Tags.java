package org.lodder.subtools.sublibrary.control;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.control.VideoPatterns.AudioEncoding;
import org.lodder.subtools.sublibrary.control.VideoPatterns.Quality;
import org.lodder.subtools.sublibrary.control.VideoPatterns.Source;
import org.lodder.subtools.sublibrary.control.VideoPatterns.VideoEncoding;

@NullMarked
public class Tags {

    private Tags() {
        // hide utility class constructor
    }

    @NullMarked
    public record Tag<T extends @Nullable Object>(String value, Function<String, T> mapper) {

        public static <T> Tag<@Nullable T> nullable(String value, Function<String, @Nullable T> mapper) {
            return new Tag<>(value, mapper);
        }
    }

    public static Tag<String> NAME = new Tag<>("name", Function.identity());
    public static Tag<String> TITLE = new Tag<>("title", Function.identity());
    public static Tag<Integer> SEASON = new Tag<>("season", Integer::parseInt);
    public static Tag<List<Integer>> EPISODE = new Tag<>("episode", v -> List.of(Integer.parseInt(v)));
    public static Tag<List<Integer>> EPISODES_TEXT = new Tag<>("episodestext", v -> {
        List<Integer> episodes = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+").matcher(v);
        while (matcher.find()) {
            episodes.add(Integer.parseInt(matcher.group()));
        }
        return List.copyOf(episodes);
    });
    public static Tag<Integer> ARABIC_NUMBER = new Tag<>("arabicnbr", Integer::parseInt);
    public static Tag<Integer> ROMAN_NUMBER = new Tag<>("romannbr", Roman::decode);
    public static Tag<Integer> YEAR = new Tag<>("year", Integer::parseInt);
    public static Tag<@Nullable AudioEncoding> AUDIO_ENCODING = Tag.nullable("audioenc", AudioEncoding::fromValue);
    public static Tag<@Nullable VideoEncoding> VIDEO_ENCODING = Tag.nullable("videoenc", VideoEncoding::fromValue);
    public static Tag<@Nullable Quality> QUALITY = Tag.nullable("quality", Quality::fromValue);
    public static Tag<@Nullable Source> SOURCE = Tag.nullable("source", Source::fromValue);
}
