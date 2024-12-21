package org.lodder.subtools.sublibrary.control;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.util.NamedPattern;

@UtilityClass
public class VideoPatterns {

    public interface VideoPatternEnumIntf {
    }

    public interface SinglePattern extends MultiplePatterns {
        @val String value;

        default String[] getValues() {
            return new String[]{ value };
        }
    }

    public interface MultiplePatterns {
        @val String[] values;
    }

    @AllArgsConstructor
    public enum Quality implements VideoPatternEnumIntf, SinglePattern {
        Q1080P("1080p"),
        Q1080I("1080i"),
        Q720P("720p"),
        Q480P("480p");

        @val @override String value;
    }

    public enum VideoEncoding implements VideoPatternEnumIntf, MultiplePatterns {
        X264("x264", "h264"),
        X265("x265", "h265");

        @val @override String[] values;

        VideoEncoding(String... values) {
            this.values = values;
        }
    }

    public enum AudioEncoding implements VideoPatternEnumIntf, MultiplePatterns {
        DD5_1("dd5.1", "dd5-1");

        @val @override String[] values;

        AudioEncoding(String... values) {
            this.values = values;
        }
    }

    public enum Source implements VideoPatternEnumIntf, MultiplePatterns {
        HDTV(false, "hdtv"),
        DVDRIP(false, "dvdrip"),
        BLURAY(false, "bluray"),
        BDRIP(false, "bdrip"),
        BRRIP(false, "brrip"),
        XVID(false, "xvid"),
        PDTV(false, "pdtv"),
        DIVX(false, "divx"),
        WEBRIP(false, "webrip"),
        RERIP(false, "rerip"),
        WEBDL(false, "webdl", "web-dl", "web.dl"),
        TS(true, "ts"),
        DVD_SCREENER(true, "dvdscreener"),
        R5(true, "r5"),
        CAM(true, "cam");

        static final Map<String, Source> VALUE_MAP = new HashMap<>();

        static {
            Source.values().forEach(source -> source.values.forEach(value -> VALUE_MAP.put(value, source)));
        }

        @val boolean manyDifferentSources;
        @val @override String[] values;

        Source(boolean manyDifferentSources, String... values) {
            this.manyDifferentSources = manyDifferentSources;
            this.values = values;
        }

        public static Source fromValue(String value) {
            return VALUE_MAP.get(value);
        }

        public boolean isTypeForValue(String value) {
            return values.stream().map(String::toLowerCase).anyMatch(value::equals);
        }

        @Override
        public String toString() {
            return values[0];
        }
    }

    @AllArgsConstructor
    public enum VideoExtensions implements SinglePattern {
        MKV("mkv"),
        MP4("mp4"),
        AVI("avi"),
        WMV("wmv"),
        TS("ts"),
        M4V("m4v");

        @val @override String value;
    }

    private static final Set<String> QUALITY_KEYWORDS_SET =
        Stream.of(Quality.values(), Source.values(), VideoEncoding.values()).map(MultiplePatterns.class::cast)
            .map(MultiplePatterns::getValues).flatMap(Arrays::stream).collect(Collectors.toSet());

    private static final Set<String> QUALITY_KEYWORDS_REGEX_SET = Set.of("web[ .-]dl", "dd5[ .]1");

    public static final Set<String> EXTENSIONS =
        VideoExtensions.values().stream().map(VideoExtensions::getValue).collect(Collectors.toSet());

    // order is important!!!!!!
    private static final String[] PATTERNS = {
        // example:
        // Back.to.the.Future.Part.II.1989.720p.BluRay.X264-AMIABLE.mkv
        "(?<${Tag.MOVIE_NAME}>['\\w\\s:&()!.,_-]+)(?<${Tag.PART}>Pt|Part|pt|part|Ep)(?<${Tag.ROMAN_EPISODE}>[I|V|X]+)" +
            "[. ](?<${Tag.YEAR}>19\\d{2}|20\\d{2})(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        "(?<${Tag.MOVIE_NAME}>['\\w\\s:&()!.,_-]+)(?<${Tag.PART}>Pt|Part|pt|part|Ep)[.](?<${Tag.ROMAN_EPISODE}>" +
            "[I|V|X]+)[. ](?<${Tag.YEAR}>19\\d{2}|20\\d{2})" +
            "(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        // The.Hunger.Games.Mockingjay.Part.1..2014.720p.BluRay.x264-SPARKS.mkv
        "(?<${Tag.MOVIE_NAME}>['\\w\\s:&()!.,_-]+)(?<${Tag.PART}>Pt|Part|pt|part|Ep)" +
            "(?<${Tag.PART_NUMBER}>[\\d]{1})[. ](?<${Tag.YEAR}>19\\d{2}|20\\d{2})" +
            "(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        "(?<${Tag.MOVIE_NAME}>['\\w\\s:&()!.,_-]+)(?<${Tag.PART}>Pt|Part|pt|part|Ep)[.]" +
            "(?<${Tag.PART_NUMBER}>[\\d]{1})[. ](?<${Tag.YEAR}>19\\d{2}|20\\d{2})" +
            "(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        // serie
        "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_-]+)[Ss. _](?<${Tag.SEASON_NUMBER}>[\\d]{1,2})[XxEe]{1,2}" +
            "(?<${Tag.EPISODE_NUMBER_START}>[\\d]{1,2})(?<${Tag.EPISODE_BETWEEN}>[XxEe][\\d]{1,2})*[XxEe]" +
            "(?<${Tag.EPISODE_NUMBER_END}>[\\d]{1,2})(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_-]+)[Ss. _](?<${Tag.SEASON_NUMBER}>[\\d]{1,2})[XxEe]{1,2}" +
            "(?<${Tag.EPISODE_NUMBER}>[\\d]{1,3})(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        // sXeX - Serienaam - Titel ex: S04E02 - White Collar - Most Wanted.mkv
        "[Ss. _](?<${Tag.SEASON_NUMBER}>[\\d]{1,2})[XxEe]{1,2}(?<${Tag.EPISODE_NUMBER_START}>[\\d]{1,2})" +
            "(?<${Tag.EPISODE_BETWEEN}>[XxEe][\\d]{1,2})*[XxEe](?<${Tag.EPISODE_NUMBER_END}>[\\d]{1,2})\\s?+-?\\s?+" +
            "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_]+)\\s?+-?\\s?+(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_]+)",
        "[Ss. _](?<${Tag.SEASON_NUMBER}>[\\d]{1,2})[XxEe]{1,2}(?<${Tag.EPISODE_NUMBER}>[\\d]{1,2})\\s?+-?\\s?+" +
            "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_]+)\\s?+-?\\s?+(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_]+)",
        // example: hawaii.five-0.2010.410.hdtv-lol.mp4
        // example:
        // Spartacus.Gods.of.The.Arena.Pt.I.720p.HDTV.X264-DIMENSION.mkv
        "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_-]+)(?<${Tag.PART}>Pt|Part|pt|part|Ep)(?<${Tag.ROMAN_EPISODE}>[I|V|X]+)" +
            "(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_-]+)(?<${Tag.PART}>Pt|Part|pt|part|Ep)[.](?<${Tag.ROMAN_EPISODE}>" +
            "[I|V|X]+)(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_-]+)(?<${Tag.PART}>Pt|Part|pt|part|Ep)(?<${Tag.EPISODE_NUMBER}>" +
            "[\\d]{1,2})(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_-]+)(?<${Tag.PART}>Pt|Part|pt|part|Ep)[.](?<${Tag.EPISODE_NUMBER}>" +
            "[\\d]{1,2})(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        // example hawaii.five-0.2010.410.hdtv-lol.mp4
        "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_-]+)[. ](?<${Tag.YEAR}>19\\d{2}|20\\d{2})[. ]" +
            "(?<${Tag.SEASON_EPISODE}>[\\d]{3,4})[. ](?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        // format movietitle.year
        "(?<${Tag.MOVIE_NAME}>['\\w\\s:&()!.,_-]+)[\\.|\\[|\\(| ]{1}(?<${Tag.YEAR}>19\\d{2}|20\\d{2})" +
            "(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        // format episode.0101.title
        // format episode.101.title
        // exclude format movietitle.720p
        "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_-]+)[. ](?<${Tag.SEASON_EPISODE}>[\\d]{3,4})[. ]" +
            "(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        // format (2-11) Joey and the High School Friend
        "[(](?<${Tag.SEASON_NUMBER}>[\\d]{1,2})[-](?<${Tag.EPISODE_NUMBER}>[\\d]{1,2})[) ]" +
            "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_-]+)[]and(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        "[(](?<${Tag.SEASON_NUMBER}>[\\d]{1,2})[-](?<${Tag.EPISODE_NUMBER}>[\\d]{1,2})[) ]" +
            "(?<${Tag.SERIE_NAME}>['\\w\\s:&()!.,_-]+)[ ]And(?<${Tag.DESCRIPTION}>['\\w\\s:&()!.,_-]+)",
        // take the rest and treat as movie
        "(?<${Tag.MOVIE_NAME}>['\\w\\s:&()!.,_-]+)[\\.|\\[|\\(| ]{1}[720P|1080P](?<${Tag.DESCRIPTION}>['\\w\\s:&()!.," +
            "_-]+)"
    };

    public static final List<NamedPattern> COMPILED_PATTERNS =
        PATTERNS.stream().map(p -> NamedPattern.compile(p, Pattern.CASE_INSENSITIVE)).toList();

    public static final List<String> QUALITY_KEYWORDS = List.of();
    // Stream.concat(QUALITY_KEYWORDS_SET.stream(),
    // new Generex(QUALITY_KEYWORDS_REGEX_SET.stream().collect(Collectors.joining("|"))).getAllMatchedStrings()
    // .stream()).toList();

    private static final String QUALITY_KEYWORDS_REGEX =
        Stream.concat(QUALITY_KEYWORDS_SET.stream(), QUALITY_KEYWORDS_REGEX_SET.stream())
            .collect(Collectors.joining("|", "(", ")"));

    public static final Pattern QUALITY_KEYWORDS_REGEX_PATTERN =
        Pattern.compile(QUALITY_KEYWORDS_REGEX, Pattern.CASE_INSENSITIVE);

}
