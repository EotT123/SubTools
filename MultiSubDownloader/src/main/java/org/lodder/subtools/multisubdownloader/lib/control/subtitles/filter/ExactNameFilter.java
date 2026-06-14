package org.lodder.subtools.multisubdownloader.lib.control.subtitles.filter;

import java.util.Map;
import java.util.regex.Pattern;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.cache.LRUMap;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public final class ExactNameFilter extends SubtitleFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExactNameFilter.class);

    private final Map<String, Pattern> patterns = new LRUMap<>(10);

    @Override
    public boolean include(Release release, Subtitle subtitle) {
        Pattern p = patterns.computeIfAbsent(release.fileNameOrName, _ ->
            Pattern.compile(release.fileNameOrName.replace(" ", "[. ]"), Pattern.CASE_INSENSITIVE));
        if (p.matcher(subtitle.fileName).find()) {
            LOGGER.debug("getSubtitlesFiltered: found EXACT match [{}] ", subtitle.fileName);
            subtitle.subtitleMatchType = SubtitleMatchType.EXACT;
            return true;
        }
        return false;
    }
}
