package org.lodder.subtools.multisubdownloader.lib.control.subtitles.filters;

import org.apache.commons.lang3.StringUtils;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleaseGroupFilter extends SubtitleFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseGroupFilter.class);

    @Override
    public boolean useSubtitle(Release release, Subtitle subtitle) {
        if (subtitle.getReleaseGroup().isEmpty()) {
            subtitle.setReleaseGroup(ReleaseParser.extractReleasegroup(subtitle.getFileName(), subtitle.getFileName().endsWith(".srt")));
        }
        if (!StringUtils.containsAnyIgnoreCase(subtitle.getReleaseGroup(), release.getReleaseGroup(), subtitle.getReleaseGroup())) {
            return false;
        }
        LOGGER.debug("getSubtitlesFiltered: found KEYWORD based TEAM match [{}] ", subtitle.getFileName());
        subtitle.setSubtitleMatchType(SubtitleMatchType.TEAM);
        return true;
    }

}
