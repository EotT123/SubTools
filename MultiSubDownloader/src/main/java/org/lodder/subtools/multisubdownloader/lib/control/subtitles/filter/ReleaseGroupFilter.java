package org.lodder.subtools.multisubdownloader.lib.control.subtitles.filter;

import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public final class ReleaseGroupFilter extends SubtitleFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseGroupFilter.class);

    @Override
    public boolean useSubtitle(Release release, Subtitle subtitle) {
        if (!Strings.CI.containsAny(subtitle.releaseGroup, release.releaseGroup, subtitle.releaseGroup)) {
            return false;
        }
        LOGGER.debug("getSubtitlesFiltered: found KEYWORD based TEAM match [{}] ", subtitle.fileName);
        subtitle.subtitleMatchType = SubtitleMatchType.TEAM;
        return true;
    }

}
