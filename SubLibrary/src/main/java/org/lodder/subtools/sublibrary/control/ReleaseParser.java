package org.lodder.subtools.sublibrary.control;

import static java.util.Objects.*;
import static org.lodder.subtools.sublibrary.control.RegexUtils.*;
import static org.lodder.subtools.sublibrary.control.Tags.*;
import static util.Utils.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.control.Roman.RomanNumeral;
import org.lodder.subtools.sublibrary.control.VideoPatterns.AudioEncoding;
import org.lodder.subtools.sublibrary.control.VideoPatterns.Quality;
import org.lodder.subtools.sublibrary.control.VideoPatterns.RegexPattern;
import org.lodder.subtools.sublibrary.control.VideoPatterns.Source;
import org.lodder.subtools.sublibrary.control.VideoPatterns.VideoEncoding;
import org.lodder.subtools.sublibrary.control.VideoPatterns.VideoExtensions;
import org.lodder.subtools.sublibrary.model.MovieReleaseWithPath;
import org.lodder.subtools.sublibrary.model.MovieReleaseWithoutPath;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.ReleaseWithPath;
import org.lodder.subtools.sublibrary.model.ReleaseWithoutPath;
import org.lodder.subtools.sublibrary.model.TvReleaseWithPath;
import org.lodder.subtools.sublibrary.model.TvReleaseWithoutPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class parses a file's name and its metadata to determine the release type (movie or TV series), creating a
 * {@link Release} object containing the metadata
 */
@NullMarked
public class ReleaseParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseParser.class);

    private ReleaseParser() {
        // hide utility class constructor
    }

    /**
     * Attempts to parse the given file using a series of regex patterns to extract relevant metadata and constructs
     * the corresponding {@link ReleaseWithPath} object (either a {@link MovieReleaseWithPath} or
     * {@link TvReleaseWithPath}).
     * The method attempts to identify and parse key details such as the release type, season, episodes, quality,
     * and release group from the filename.
     *
     * @param file The file path being parsed.
     * @param includeParent If true, the parent directory name is also parsed.
     * @return the {@link ReleaseWithPath} if parsing was successful; otherwise null
     */
    public static @Nullable ReleaseWithPath parse(Path file, boolean includeParent=false) {
        ReleaseWithoutPath release = includeParent ? parse(file.getParent().fileNameAsString) : null;
        if (release == null) {
            release = parse(StringUtils.substringBeforeLast(file.fileNameAsString, "."));
        }
        return ifNotNull(release, r -> withPath(r, file));
    }

    public static ReleaseWithPath withPath(ReleaseWithoutPath release, Path path) {
        return switch (release) {
            case TvReleaseWithoutPath tvRelease -> new TvReleaseWithPath(tvRelease, path);
            case MovieReleaseWithoutPath movieRelease -> new MovieReleaseWithPath(movieRelease, path);
        };
    }

    /**
     * Attempts to parse the given value using a series of regex patterns to extract relevant metadata and constructs
     * the corresponding {@link ReleaseWithoutPath} object (either a {@link MovieReleaseWithoutPath} or
     * {@link TvReleaseWithoutPath}).
     * <br>
     * The method attempts to identify and parse key details such as the release type, season, episodes, quality,
     * and release group from the filename.
     *
     * @param text value to be parsed.
     * @return the {@link ReleaseWithoutPath} if parsing was successful; otherwise null
     */
    public static @Nullable ReleaseWithoutPath parse(String text) {
        ParserResults parserResults = new ParserResults(Strings.CS.endsWithAny(text, ".zip", ".srt") ||
            Strings.CS.endsWithAny(text,
                VideoExtensions.values().stream().map(VideoExtensions::getValue).toList().toArray(new String[0])) ?
            StringUtils.substringBeforeLast(text, ".") : text);

        parseReleaseType(parserResults);
        String quality = String.join(" ", getQualityKeyWordsAlreadyParsed(parserResults));

        // When quality parts are found, the filename is split into multiple parts. Consider the last part the
        // release group
        String releaseGroup = parserResults.parts.size() > 1 ? parserResults.parts.last : "";
        if (StringUtils.isNotBlank(releaseGroup)) {
            parserResults.removeLastPart();
        }

        // Parse the number part, but don't remove it from the remaining parts, as it should be included in the title
        // for movies.
        parserResults.parseWithoutRemoving(part_number_Regex(NumberType.ARABIC), part_number_Regex(NumberType.ROMAN));

        // Parse the season + episode numbers (using format SxxExx), and the title
        parserResults.parse(seasonSxxExx_name_titleRegex(), name_season_episode_title_Regex(SeasonEpisodeType.SXXEXX));

        if (parserResults.containsNone(SEASON, EPISODES_TEXT)) {
            if (parserResults.containsNone(ARABIC_NUMBER, ROMAN_NUMBER)) {
                // If the season and episode numbers are not found, and neither are the Arabic nor Roman numbers, try to
                // parse the season + episode numbers (using less 'safe' formats 'see' and 's_ee'), and the title
                parserResults.parse(name_season_episode_title_Regex(SeasonEpisodeType.X_XX),
                    name_season_episode_title_Regex(SeasonEpisodeType.XXX),
                    season_episode_name_title_Regex(SeasonEpisodeType.X_XX),
                    season_episode_name_title_Regex(SeasonEpisodeType.XXX));
            }
            if (parserResults.containsNone(SEASON, EPISODES_TEXT, EPISODE)) {
                // If still no episode numbers are found, try to parse the year
                parserResults.parse(yearRegex());

                // If the year is found, or the source is likely a movie release and not a TV show release, create a
                // MovieReleaseWithoutPath object
                if (parserResults.contains(YEAR) || parserResults.getNamedMatch(SOURCE).stream()
                    .anyMatch(source -> source.likelyMovieRelease || !source.likelyTvRelease)) {
                    if (Strings.CS.equals(parserResults.parts.first, text)) {
                        return null;
                    }
                    return new MovieReleaseWithoutPath(
                        name:cleanUnwantedChars(parserResults.parts.first),
                        year:parserResults.getNamedMatchValue(YEAR),
                        releaseGroup:releaseGroup,
                        quality:StringUtils.toRootLowerCase(quality),
                        completeName:text);
                }
            }
        }

        // the file is considered a tv show at this point.

        int season;
        LinkedHashSet<Integer> episodes = new LinkedHashSet<>();
        if (!parserResults.contains(SEASON) || (parserResults.containsNone(EPISODE, EPISODES_TEXT))) {
            if (parserResults.containsNone(ARABIC_NUMBER, ROMAN_NUMBER)) {
                return null;
            }
            // Parse the number parts. They are still present at this point, because they were not removed from the
            // remaining parts earlier
            parserResults.parse(part_number_Regex(NumberType.ARABIC), part_number_Regex(NumberType.ROMAN));
            // When using the part numbers, assume only one season exists for the TV show
            season = 1;
            episodes.add(requireNonNull(parserResults.getNamedMatchValue(ARABIC_NUMBER, ROMAN_NUMBER)));
        } else {
            season = requireNonNull(parserResults.getNamedMatchValue(SEASON));
            episodes.addAll(requireNonNull(parserResults.getNamedMatchValue(EPISODE, EPISODES_TEXT)));
        }

        // if no serie name was yet found, use the first remaining part as the serie name
        String name = parserResults.containsNone(NAME) ?
            parserResults.parts.first : requireNonNull(parserResults.getNamedMatchValue(NAME));
        // create a new parser to parse a potential year in the title (only at the end of the name)
        parserResults.createWithNewText(name)
            .parse(Regex.builder()
                .startOfText()
                .tag(NAME)
                .regex(".*")
                .regex(DELIMITER)
                .regexOptional("\\(")
                .regex(yearRegex().create())
                .regexOptional("\\)")
                .endOfText());
        name = parserResults.containsNone(NAME) ? parserResults.parts.first : parserResults.getNamedMatchValue(NAME);

        if (Strings.CS.equals(name, text)) {
            return null;
        }
        return new TvReleaseWithoutPath(
            name:cleanUnwantedChars(name),
            season:season,
            episodes:episodes,
            title:cleanUnwantedChars(parserResults.getNamedMatchValue(TITLE)),
            releaseGroup:releaseGroup,
            special:isSpecialEpisode(season, episodes),
            quality:StringUtils.toRootLowerCase(quality),
            completeName:text);
    }

    @NullMarked
    enum SeasonEpisodeType {
        SXXEXX(Regex.builder()
            .regex("s")
            .tag(SEASON).regex("\\d{1,2}")
            .tag(EPISODES_TEXT).regex("([xe]\\d{1,2})*")),
        XXX(Regex.builder()
            .tag(SEASON).regex("\\d")
            .tag(EPISODES_TEXT).regex("\\d{2}")),
        X_XX(Regex.builder()
            .regex("\\(")
            .tag(SEASON).regex("\\d{1,2}")
            .regex("-")
            .tag(EPISODE).regex("\\d{2}")
            .regex("\\)"));

        @val RegexNext regex;

        SeasonEpisodeType(RegexNext regex) {
            this.regex = regex;
        }
    }

    @NullMarked
    enum NumberType {
        ARABIC(Regex.builder()
            .tag(ARABIC_NUMBER).regex("\\d{1,2}")),
        ROMAN(Regex.builder()
            .tag(ROMAN_NUMBER)
            .regex("[" + RomanNumeral.values().stream().map(RomanNumeral::name).collect(Collectors.joining()) + "]+"));

        @val RegexNext regex;

        NumberType(RegexNext regex) {
            this.regex = regex;
        }
    }

    /**
     * Returns a regular expression pattern for matching years (e.g., 1990, 2023).
     *
     * @return A RegexNext object representing the year regex.
     */
    private static RegexNext yearRegex() {
        return Regex.builder().tag(YEAR).regex("19\\d{2}|20\\d{2}");
    }

    private static String name_season_episode_title_Regex(SeasonEpisodeType seasonEpisodeType) {
        return Regex.builder()
            .startOfText()
            .tag(NAME)
            .regex(".*")
            .regex(DELIMITER)
            .regex(seasonEpisodeType.regex.create())
            .regexOptional(DELIMITER + titleRegex().create())
            .endOfText()
            .create();
    }

    private static String seasonSxxExx_name_titleRegex() {
        return Regex.builder()
            .startOfText()
            .regexOptional("\\(")
            .regex(SeasonEpisodeType.SXXEXX.regex.create())
            .regexOptional("\\)")
            .regex(DELIMITER)
            .tag(NAME).regex(".*")
            .regex(DELIMITER)
            .regex("-")
            .regex(DELIMITER)
            .tag(TITLE).regex(".*")
            .endOfText()
            .create();
    }

    private static String season_episode_name_title_Regex(SeasonEpisodeType seasonEpisodeType) {
        return Regex.builder()
            .startOfText()
            .regex(seasonEpisodeType.regex.create())
            .tag(NAME).regex(".*")
            .regexOptional(DELIMITER + titleRegex().create())
            .endOfText()
            .create();
    }

    /**
     * Helper method to generate a title regex.
     *
     * @return A RegexNext object for matching a title.
     */
    private static RegexNext titleRegex() {
        return Regex.builder().tag(TITLE).regex(".*");
    }

    private static RegexNext part_number_Regex(NumberType numberType) {
        return Regex.builder().regex("(pt|part|ep)").regex(DELIMITER).regex(numberType.regex.create());
    }


    private static void parseReleaseType(ParserResults parseResults) {
        parseResults.parse(Regex.builder().tag(QUALITY).regex(Quality.class, Quality::getRegex));
        parseResults.parse(Regex.builder().tag(SOURCE).regex(Source.class, Source::getRegex));
        parseResults.parse(Regex.builder().tag(AUDIO_ENCODING).regex(AudioEncoding.class, AudioEncoding::getRegex));
        parseResults.parse(Regex.builder().tag(VIDEO_ENCODING).regex(VideoEncoding.class, VideoEncoding::getRegex));
    }

    @NullMarked
    static class ParserResults {
        @val List<String> parts = new ArrayList<>();
        private final NamedMatches namedMatches;

        public ParserResults(String text) {
            this(text, new NamedMatches());
        }

        private ParserResults(String text, NamedMatches namedMatches) {
            parts.add(text);
            this.namedMatches = namedMatches;
        }

        public ParserResults createWithNewText(String text) {
            return new ParserResults(text, namedMatches);
        }

        public boolean parse(RegexBuilderBuild... regexBuilders) {
            return parse(true, regexBuilders);
        }

        public boolean parseWithoutRemoving(RegexBuilderBuild... regexBuilders) {
            return parse(false, regexBuilders);
        }

        public boolean parse(String... regexes) {
            return parse(true, regexes);
        }

        public boolean parseWithoutRemoving(String... regexes) {
            return parse(false, regexes);
        }

        private boolean parse(boolean removeMatchedParts, RegexBuilderBuild... regexBuilders) {
            boolean result = false;
            Multimap<String, String> matches = MultimapBuilder.hashKeys().arrayListValues().build();
            for (RegexBuilderBuild regexBuilder : regexBuilders) {
                result |=
                    regexBuilder.createWithDelimiter().stream().map(v -> parsePrivate(matches, v, removeMatchedParts))
                        .toList().contains(true);
            }
            matches.asMap().forEach(namedMatches::put);
            return result;
        }

        private boolean parse(boolean removeMatchedParts, String... regexes) {
            Multimap<String, String> matches = MultimapBuilder.hashKeys().arrayListValues().build();
            boolean result =
                regexes.stream().map(regex -> parsePrivate(matches, regex, removeMatchedParts)).toList().contains(true);
            matches.asMap().forEach(namedMatches::put);
            return result;
        }

        public boolean parsePrivate(Multimap<String, String> matches, String regex, boolean removeMatchedParts) {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            for (String part : parts) {
                Matcher matcher = pattern.matcher(part);
                if (matcher.find()) {
                    Map<String, Integer> namedGroupMap = matcher.namedGroups();
                    namedGroupMap.entrySet().forEach(entry -> matches.put(entry.key, matcher.group(entry.value)));
                    String match = matcher.group();
                    if (removeMatchedParts) {
                        List<String> remainingParts = parts.stream()
                            .flatMap(p -> p.split(Pattern.quote(match)).stream().filter(StringUtils::isNotBlank))
                            .toList();
                        parts.clear();
                        parts.addAll(remainingParts);
                    }
                    return true;
                }
            }
            return false;
        }

        public void removeLastPart() {
            parts.removeLast();
        }

        public boolean contains(Tag<?> tag) {
            return !getNamedMatch(tag).isEmpty();
        }

        public boolean containsNone(Tag<?>... tags) {
            return tags.stream().noneMatch(this::contains);
        }

        @SafeVarargs
        public final <T> @Nullable T getNamedMatchValue(Tag<T>... tags) {
            for (Tag<T> tag : tags) {
                List<T> namedMatch = getNamedMatch(tag, tag.mapper);
                if (!namedMatch.isEmpty()) {
                    return namedMatch.first;
                }
            }
            return null;
        }

        public <T> List<T> getNamedMatch(Tag<T> tag) {
            return getNamedMatch(tag, tag.mapper);
        }

        public <T> List<T> getNamedMatch(Tag<T> tag, Function<String, T> mapper) {
            List<String> values = namedMatches.get(tag);
            return values == null ? List.of() : values.stream().map(mapper).distinct().toList();
        }
    }


    /**
     * Cleans unwanted characters from a string (e.g., underscores, extra spaces).
     *
     * @param text The input text to clean.
     * @return The cleaned text.
     */
    private static String cleanUnwantedChars(@Nullable String text) {
        if (text == null) {
            return "";
        }
        String newText = text;
        newText = newText.replace("cd1", " ").replace("cd2", " ");
        newText = newText.replace(".", " "); // remove point bones.01x01
        newText = newText.replace("_", " "); // remove underscore bones_01x01
        newText = newText.replace(" -", " "); // remove space dash "ncis - 01x01"
        newText = newText.replace(":", ""); // remove double point "CSI: NY"
        newText = newText.replace("(", ""); // remove ( for castle (2009)
        newText = newText.replace(")", ""); // remove ) for castle (2009)
        newText = newText.replace("'", "");
        if (newText.startsWith("- ")) {
            newText = newText.substring(2);
        }

        if (newText.endsWith("-")) { // implemented if for "hawaii five-0"
            newText = newText.replace("-", ""); // remove space dash "altiplano-cd1"
        }

        // remove multiple spaces between text Back to the Future[][]Part II
        newText = newText.replaceAll(" +", " ");

        return newText.trim();
    }

    public static String getQualityKeyword(String name) {
        return String.join(" ", getQualityKeyWords(name));
    }

    public static List<String> getQualityKeyWords(String name) {
        ParserResults parserResults = new ParserResults(name);
        parseReleaseType(parserResults);
        return getQualityKeyWordsAlreadyParsed(parserResults);
    }

    public static ReleaseParserExtraInfo parseExtraInfo(String text) {
        return ReleaseParserExtraInfo.parseExtraInfo(text);
    }

    @NullMarked
    public static class ReleaseParserExtraInfo {

        private final String text;
        private final ParserResults parserResults;

        private ReleaseParserExtraInfo(String extraInfo) {
            String info = Strings.CS.endsWithAny(extraInfo, ".srt", ".zip", ".rar") ?
                extraInfo.substring(0, extraInfo.length() - 4) : extraInfo;
            info = Strings.CI.replace(info, "REPACK.", "");
            info = Strings.CI.replace(info, "REPACK-", "");
            info = Strings.CI.replace(info, "INTERNAL.", "");
            info = Strings.CI.replace(info, "INTERNAL-", "");
            this.text = info;
            this.parserResults = new ParserResults(extraInfo);
            parseReleaseType(parserResults);
        }

        public static ReleaseParserExtraInfo parseExtraInfo(String text) {
            return new ReleaseParserExtraInfo(text);
        }

        public String getQualityKeyword() {
            return String.join(" ", getQualityKeyWordsAlreadyParsed(parserResults));
        }

        public @Nullable String getReleaseGroupBestEffort() {
            if (parserResults.parts.isEmpty()) {
                return null;
            }
            if (Strings.CI.contains(text, "retail")) {
                return "RETAIL";
            }
            return parserResults.parts.getLast();
        }

        public List<String> getRemainingParts() {
            return parserResults.parts;
        }

        public String getRemainingPartsAsString() {
            return String.join(" ", parserResults.parts);
        }
    }


    private static List<String> getQualityKeyWordsAlreadyParsed(ReleaseParser.ParserResults parserResults) {
        return Stream.of(
                parserResults.getNamedMatch(QUALITY),
                parserResults.getNamedMatch(SOURCE),
                parserResults.getNamedMatch(AUDIO_ENCODING),
                parserResults.getNamedMatch(VIDEO_ENCODING))
            .flatMap(List::stream).map(RegexPattern::getValue).toList();
    }

    public static String extractReleaseGroup(final String fileName, boolean hasExtension) {
        LOGGER.trace("extractReleaseGroup: name: {} , hasExtension: {}", fileName, hasExtension);
        Pattern releaseGroupPattern;
        if (hasExtension) {
            releaseGroupPattern = Pattern.compile("-(\\w+)\\.\\w+$");
        } else {
            releaseGroupPattern = Pattern.compile("-(\\w+)$");
        }
        Matcher matcher = releaseGroupPattern.matcher(fileName);
        String releaseGroup = "";
        if (matcher.find()) {
            releaseGroup = matcher.group(1);
        }

        LOGGER.trace("extractReleaseGroup: release group: {}", releaseGroup);
        return releaseGroup;
    }

    public static boolean isSpecialEpisode(final int season, final LinkedHashSet<Integer> episodeNumbers) {
        return season == 0 || (episodeNumbers.size() == 1 && episodeNumbers.first == 0);
    }

    /**
     * Helper class for storing and retrieving named regular expression matches.
     */
    @NullMarked
    private static class NamedMatches {
        private final Map<String, List<String>> map = new HashMap<>();

        public void put(String key, Collection<String> values) {
            map.put(key, values.stream().distinct().toList());
        }

        public @Nullable List<String> get(Tag<?> tag) {
            return map.get(tag.value);
        }
    }
}