package org.lodder.subtools.sublibrary.control;

import static org.lodder.subtools.sublibrary.control.Tag.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import extensions.java.nio.file.Path.PathExt;
import org.lodder.subtools.sublibrary.exception.ReleaseParseException;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.util.NamedMatcher;
import org.lodder.subtools.sublibrary.util.NamedPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleaseParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseParser.class);

    private NamedMatcher namedMatcher;

    public final Release parse(Path file) throws ReleaseParseException {
        String folderName = file.getParent() != null ? file.getParent().getFileName().toString() : "";

        for (String fileParseName : List.of(file.fileName.toString(), folderName)) {
            for (NamedPattern np : VideoPatterns.COMPILED_PATTERNS) {
                namedMatcher = np.matcher(fileParseName);
                if (namedMatcher.find()) {
                    LOGGER.trace("Parsing match found using file name: {}", fileParseName);
                    return parsePatternResult(file, fileParseName);
                }
            }
        }
        throw new ReleaseParseException("Unknown format, can't be parsed: ${file.toAbsolutePath()}");
    }

    private Release parsePatternResult(Path file, String fileParseName) throws ReleaseParseException {
        List<String> namedGroups = namedMatcher.parentPattern.groupNames;
        String seriesName = "";
        List<Integer> episodeNumbers = new ArrayList<>();
        int seasonNumber = 0;
        Integer year = namedGroups.contains(YEAR) ? Integer.parseInt(namedMatcher.group(YEAR)) : null;
        String description = namedGroups.contains(DESCRIPTION) ? namedMatcher.group(DESCRIPTION).substring(1) : "";

        if (namedGroups.contains(MOVIE_NAME)) {
            String movieName;
            if (namedGroups.contains(PART)) {
                String number = "";
                if (namedGroups.contains(ROMAN_EPISODE)) {
                    number = namedMatcher.group(ROMAN_EPISODE);
                } else if (namedGroups.contains(PART_NUMBER)) {
                    number = namedMatcher.group(PART_NUMBER);
                }
                movieName =
                    cleanUnwantedChars(namedMatcher.group(MOVIE_NAME) + " " + namedMatcher.group(PART) + " " + number);
            } else {
                movieName = cleanUnwantedChars(namedMatcher.group(MOVIE_NAME));
            }
            return MovieRelease.builder()
                .name(movieName)
                .file(file)
                .year(year)
                .description(description)
                .releaseGroup(extractReleaseGroup(file.getFileName().toString(), true))
                .quality(getQualityKeyword(fileParseName))
                .build();
        }

        if (namedGroups.contains(EPISODE_NUMBER_1)) {
            String value = namedMatcher.group(EPISODE_NUMBER_1);
            LOGGER.trace("parsePatternResult: {}: {}", EPISODE_NUMBER_1, value);
            // Multiple episodes, have episodenumber1, 2 ....
            for (String group : namedGroups) {
                Pattern pattern = Pattern.compile(EPISODE_NUMBER + "(\\d+)");
                Matcher match = pattern.matcher(group);
                if (match.matches()) {
                    episodeNumbers.add(Integer.parseInt(namedMatcher.group(group)));
                }
            }
            Collections.sort(episodeNumbers);
        } else if (namedGroups.contains(EPISODE_NUMBER_START)) {
            String episodeNumberStart = namedMatcher.group(EPISODE_NUMBER_START);
            LOGGER.trace("parsePatternResult: namedGroups: {}", namedGroups);
            LOGGER.trace("parsePatternResult: {}: {}", EPISODE_NUMBER_START, episodeNumberStart);
            // Multiple episodes, regex specifies start and end number
            int start = Integer.parseInt(episodeNumberStart);
            int end = Integer.parseInt(namedMatcher.group(EPISODE_NUMBER_END));
            if (start > end) {
                int temp = start;
                start = end;
                end = temp;
            }
            IntStream.rangeClosed(start, end).forEach(episodeNumbers::add);
        } else if (namedGroups.contains(EPISODE_NUMBER)) {
            String value = namedMatcher.group(EPISODE_NUMBER);
            LOGGER.trace("parsePatternResult: {}: {}", EPISODE_NUMBER, value);
            episodeNumbers.add(Integer.parseInt(value));
        } else if (namedGroups.contains(YEAR) || namedGroups.contains(MONTH) || namedGroups.contains(DAY)) {
            // TODO: need to implement
        } else if (namedGroups.contains(ROMAN_EPISODE) && !namedGroups.contains(YEAR)) {
            episodeNumbers.add(Roman.decode(namedMatcher.group(ROMAN_EPISODE)));
        }

        if (namedGroups.contains(SERIE_NAME)) {
            String value = namedMatcher.group(SERIE_NAME);
            LOGGER.trace("parsePatternResult: {}: {}", SERIE_NAME, value);
            seriesName = cleanUnwantedChars(value);
            if (namedGroups.contains(YEAR)) {
                seriesName = seriesName + " " + namedMatcher.group(YEAR);
            }
        }

        if (namedGroups.contains(SEASON_NUMBER)) {
            String value = namedMatcher.group(SEASON_NUMBER);
            LOGGER.trace("parsePatternResult: {}: {}", SEASON_NUMBER, value);
            seasonNumber = Integer.parseInt(value);
        } else if (namedGroups.contains(PART) && !namedGroups.contains(YEAR)) {
            seasonNumber = 1;
        } else if (namedGroups.contains(YEAR) && namedGroups.contains(MONTH) && namedGroups.contains(DAY)) {
            // need to implement
        } else if (namedGroups.contains(SEASON_EPISODE)) {
            String value = namedMatcher.group(SEASON_EPISODE);
            LOGGER.trace("parsePatternResult: season_episode: {}", value);
            if (value.length() == 3) {
                episodeNumbers.add(Integer.parseInt(value.substring(1, 3)));
                seasonNumber = Integer.parseInt(value.substring(0, 1));
            } else if (value.length() == 4) {
                episodeNumbers.add(Integer.parseInt(value.substring(2, 4)));
                seasonNumber = Integer.parseInt(value.substring(0, 2));
            }
        } else {
            // No season number specified, usually for Anime
            // TODO: need to implement
            throw new ReleaseParseException("Unable to parse the namedmatcher");
        }
        return TvRelease.builder()
            .name(seriesName)
            .season(seasonNumber)
            .episodes(episodeNumbers)
            .file(file)
            .description(PathExt.withoutExtension(description))
            .releaseGroup(extractReleaseGroup(file.getFileName().toString(), true))
            .special(isSpecialEpisode(seasonNumber, episodeNumbers))
            .quality(getQualityKeyword(fileParseName))
            .build();
    }

    private String cleanUnwantedChars(String text) {
        String newText = text;
        if (newText.contains("cd1")) {
            newText = newText.replace("cd1", " ");
        }
        if (newText.contains("cd2")) {
            newText = newText.replace("cd2", " ");
        }

        newText = newText.replace(".", " "); // remove point bones.01x01
        newText = newText.replace("_", " "); // remove underscore bones_01x01
        newText = newText.replace(" -", " "); // remove space dash "ncis - 01x01"
        newText = newText.replace(":", ""); // remove double point "CSI: NY"
        newText = newText.replace("(", ""); // remove ( for castle (2009)
        newText = newText.replace(")", ""); // remove ) for castle (2009)
        newText = newText.replace("'", "");

        if (newText.endsWith("-")) { // implemented if for "hawaii five-0"
            newText = newText.replace("-", ""); // remove space dash "altiplano-cd1"
        }

        // remove multiple spaces between text Back to the Future[][]Part II
        newText = newText.replaceAll(" +", " ");

        return newText.trim();
    }

    public static String getQualityKeyword(String name) {
        LOGGER.trace("getQualityKeyword: name: {}", name);
        Matcher m = VideoPatterns.QUALITY_KEYWORDS_REGEX_PATTERN.matcher(name.trim().toLowerCase());
        StringBuilder builder = new StringBuilder();
        while (m.find()) {
            builder.append(m.group().replace(".", " ")).append(" ");
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getQualityKeyWords: keyswords: {}", builder.toString().trim());
        }
        return builder.toString().trim();
    }

    public static List<String> getQualityKeyWords(String name) {
        LOGGER.trace("getQualityKeyWords: name: {}", name);
        Matcher m = VideoPatterns.QUALITY_KEYWORDS_REGEX_PATTERN.matcher(name.trim().toLowerCase());
        List<String> keywords = new ArrayList<>();
        while (m.find()) {
            keywords.add(m.group());
        }
        LOGGER.trace("getQualityKeyWords: keywords: {}", keywords);
        return keywords;
    }

    public static String extractReleaseGroup(final String fileName, boolean hasExtension) {
        LOGGER.trace("extractReleaseGroup: name: {} , hasExtension: {}", fileName, hasExtension);
        Pattern releaseGroupPattern;
        if (hasExtension) {
            releaseGroupPattern = Pattern.compile("-([\\w]+).[\\w]+$");
        } else {
            releaseGroupPattern = Pattern.compile("-([\\w]+)$");
        }
        Matcher matcher = releaseGroupPattern.matcher(fileName);
        String releaseGroup = "";
        if (matcher.find()) {
            releaseGroup = matcher.group(1);
        }

        LOGGER.trace("extractReleaseGroup: release group: {}", releaseGroup);
        return releaseGroup;
    }

    public static boolean isSpecialEpisode(final int season, final List<Integer> episodeNumbers) {
        if (season == 0) {
            return true;
        }
        return episodeNumbers.size() == 1 && episodeNumbers.first == 0;
    }
}
