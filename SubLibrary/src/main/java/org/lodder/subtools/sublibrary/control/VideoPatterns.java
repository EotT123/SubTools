package org.lodder.subtools.sublibrary.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.lodder.subtools.sublibrary.util.NamedPattern;

import com.mifmif.common.regex.Generex;

public class VideoPatterns {

    private List<NamedPattern> compiledPatterns = null;
    private List<String> keys = null;
    private String qualityRegex = null;

    protected static final String[] QUALITYKEYWORDS = { "hdtv", "dvdrip", "bluray",
            "1080p", "ts", "dvdscreener", "r5", "bdrip", "brrip", "720p", "xvid", "cam", "480p", "x264",
            "1080i", "pdtv", "divx", "webrip", "h264", "rerip", "webdl" };

    protected static final String[] QUALITYREGEXKEYWORDS = { "web[ .-]dl", "dd5[ .]1" };

    public static final String[] EXTENSIONS = { "avi", "mkv", "wmv", "ts", "mp4", "m4v" };

    // order is important!!!!!!
    protected final static String[] PATTERNS =
            {
            // example:
            // Back.to.the.Future.Part.II.1989.720p.BluRay.X264-AMIABLE.mkv
            "(?<moviename>[\'\\w\\s:&()!.,_-]+)(?<part>Pt|Part|pt|part|Ep)(?<romanepisode>[I|V|X]+)[. ](?<year>19\\d{2}|20\\d{2})(?<description>[\'\\w\\s:&()!.,_-]+)",
            "(?<moviename>[\'\\w\\s:&()!.,_-]+)(?<part>Pt|Part|pt|part|Ep)[.](?<romanepisode>[I|V|X]+)[. ](?<year>19\\d{2}|20\\d{2})(?<description>[\'\\w\\s:&()!.,_-]+)",
            // The.Hunger.Games.Mockingjay.Part.1..2014.720p.BluRay.x264-SPARKS.mkv
            "(?<moviename>[\'\\w\\s:&()!.,_-]+)(?<part>Pt|Part|pt|part|Ep)(?<partnumber>[\\d]{1})[. ](?<year>19\\d{2}|20\\d{2})(?<description>[\'\\w\\s:&()!.,_-]+)",
            "(?<moviename>[\'\\w\\s:&()!.,_-]+)(?<part>Pt|Part|pt|part|Ep)[.](?<partnumber>[\\d]{1})[. ](?<year>19\\d{2}|20\\d{2})(?<description>[\'\\w\\s:&()!.,_-]+)",
            // serie
            "(?<seriesname>[\'\\w\\s:&()!.,_-]+)[Ss. _](?<seasonnumber>[\\d]{1,2})[XxEe]{1,2}(?<episodenumberstart>[\\d]{1,2})(?<episodebetween>[XxEe][\\d]{1,2})*[XxEe](?<episodenumberend>[\\d]{1,2})(?<description>[\'\\w\\s:&()!.,_-]+)",
            "(?<seriesname>[\'\\w\\s:&()!.,_-]+)[Ss. _](?<seasonnumber>[\\d]{1,2})[XxEe]{1,2}(?<episodenumber>[\\d]{1,3})(?<description>[\'\\w\\s:&()!.,_-]+)",
            // sXeX - Serienaam - Titel ex: S04E02 - White Collar - Most Wanted.mkv
            "[Ss. _](?<seasonnumber>[\\d]{1,2})[XxEe]{1,2}(?<episodenumberstart>[\\d]{1,2})(?<episodebetween>[XxEe][\\d]{1,2})*[XxEe](?<episodenumberend>[\\d]{1,2})\\s?+-?\\s?+(?<seriesname>[\'\\w\\s:&()!.,_]+)\\s?+-?\\s?+(?<description>[\'\\w\\s:&()!.,_]+)",
            "[Ss. _](?<seasonnumber>[\\d]{1,2})[XxEe]{1,2}(?<episodenumber>[\\d]{1,2})\\s?+-?\\s?+(?<seriesname>[\'\\w\\s:&()!.,_]+)\\s?+-?\\s?+(?<description>[\'\\w\\s:&()!.,_]+)",
            // example: hawaii.five-0.2010.410.hdtv-lol.mp4
            // example:
            // Spartacus.Gods.of.The.Arena.Pt.I.720p.HDTV.X264-DIMENSION.mkv
            "(?<seriesname>[\'\\w\\s:&()!.,_-]+)(?<part>Pt|Part|pt|part|Ep)(?<romanepisode>[I|V|X]+)(?<description>[\'\\w\\s:&()!.,_-]+)",
            "(?<seriesname>[\'\\w\\s:&()!.,_-]+)(?<part>Pt|Part|pt|part|Ep)[.](?<romanepisode>[I|V|X]+)(?<description>[\'\\w\\s:&()!.,_-]+)",
            "(?<seriesname>[\'\\w\\s:&()!.,_-]+)(?<part>Pt|Part|pt|part|Ep)(?<episodenumber>[\\d]{1,2})(?<description>[\'\\w\\s:&()!.,_-]+)",
            "(?<seriesname>[\'\\w\\s:&()!.,_-]+)(?<part>Pt|Part|pt|part|Ep)[.](?<episodenumber>[\\d]{1,2})(?<description>[\'\\w\\s:&()!.,_-]+)",
            // example hawaii.five-0.2010.410.hdtv-lol.mp4
            "(?<seriesname>[\'\\w\\s:&()!.,_-]+)[. ](?<year>19\\d{2}|20\\d{2})[. ](?<season_episode>[\\d]{3,4})[. ](?<description>[\'\\w\\s:&()!.,_-]+)",
            // format movietitle.year
            "(?<moviename>[\'\\w\\s:&()!.,_-]+)[\\.|\\[|\\(| ]{1}(?<year>19\\d{2}|20\\d{2})(?<description>[\'\\w\\s:&()!.,_-]+)",
            // format episode.0101.title
            // format episode.101.title
            // exclude format movietitle.720p
            "(?<seriesname>[\'\\w\\s:&()!.,_-]+)[. ](?<season_episode>[\\d]{3,4})[. ](?<description>[\'\\w\\s:&()!.,_-]+)",
            // format (2-11) Joey and the High School Friend
            "[(](?<seasonnumber>[\\d]{1,2})[-](?<episodenumber>[\\d]{1,2})[) ](?<seriesname>[\'\\w\\s:&()!.,_-]+)[ ]and(?<description>[\'\\w\\s:&()!.,_-]+)",
            "[(](?<seasonnumber>[\\d]{1,2})[-](?<episodenumber>[\\d]{1,2})[) ](?<seriesname>[\'\\w\\s:&()!.,_-]+)[ ]And(?<description>[\'\\w\\s:&()!.,_-]+)",
            // take the rest and treat as movie
            "(?<moviename>[\'\\w\\s:&()!.,_-]+)[\\.|\\[|\\(| ]{1}[720P|1080P](?<description>[\'\\w\\s:&()!.,_-]+)"

    };

    public VideoPatterns() {
        buildQualityKeywordsList();
        buildCompiledPatternList();
        buildQualityKeywordsRegex();
    }

    public List<NamedPattern> getCompiledPatterns() {
        return compiledPatterns;
    }

    public String getQualityKeysRegex() {
        return qualityRegex;
    }

    public List<String> getQualityKeywords() {
        return keys;
    }

    private void buildQualityKeywordsList() {
        keys = new ArrayList<>();
        Collections.addAll(keys, QUALITYKEYWORDS);
        keys.addAll(getQualityRegexKeywords());
    }

    private void buildCompiledPatternList() {
        compiledPatterns = new ArrayList<>();
        for (String p : PATTERNS) {
            compiledPatterns.add(NamedPattern.compile(p, Pattern.CASE_INSENSITIVE));
        }
    }

    private void buildQualityKeywordsRegex() {
        StringBuilder sb = new StringBuilder();
        String separator = "|";

        sb.append("(");

        if (VideoPatterns.QUALITYKEYWORDS.length > 0) {
            sb.append(VideoPatterns.QUALITYKEYWORDS[0]);
            for (int i = 1; i < VideoPatterns.QUALITYKEYWORDS.length; i++) {
                sb.append(separator);
                sb.append(VideoPatterns.QUALITYKEYWORDS[i]);
            }
        }

        if (VideoPatterns.QUALITYREGEXKEYWORDS.length > 0) {
            for (String element : VideoPatterns.QUALITYREGEXKEYWORDS) {
                sb.append(separator);
                sb.append(element);
            }
        }

        sb.append(")");
        qualityRegex = sb.toString();
    }

    private List<String> getQualityRegexKeywords() {
        List<String> keys;

        StringBuilder regex = new StringBuilder();
        String separator = "|";
        if (VideoPatterns.QUALITYREGEXKEYWORDS.length > 0) {
            regex.append(VideoPatterns.QUALITYREGEXKEYWORDS[0]);
            for (int i = 1; i < VideoPatterns.QUALITYREGEXKEYWORDS.length; i++) {
                regex.append(separator);
                regex.append(VideoPatterns.QUALITYREGEXKEYWORDS[i]);
            }
        }

        Generex generex = new Generex(regex.toString());

        return generex.getAllMatchedStrings();
    }

}
