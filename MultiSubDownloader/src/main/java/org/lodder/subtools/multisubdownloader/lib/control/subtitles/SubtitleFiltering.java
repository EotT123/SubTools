package org.lodder.subtools.multisubdownloader.lib.control.subtitles;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.lib.control.subtitles.filter.ExactNameFilter;
import org.lodder.subtools.multisubdownloader.lib.control.subtitles.filter.KeywordFilter;
import org.lodder.subtools.multisubdownloader.lib.control.subtitles.filter.ReleaseGroupFilter;
import org.lodder.subtools.multisubdownloader.lib.control.subtitles.filter.SubtitleFilter;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;

@NullMarked
public class SubtitleFiltering {

    private final SubtitleFilter exactNameFilter = new ExactNameFilter();
    private final SubtitleFilter keywordFilter = new KeywordFilter();
    private final SubtitleFilter releaseGroupFilter = new ReleaseGroupFilter();

    public boolean useSubtitle(Subtitle subtitle, Release release) {
        return !excludeSubtitle(subtitle, release);
    }

    public boolean excludeSubtitle(Subtitle subtitle, Release release) {
        return excludeSubtitleHearingImpaired(subtitle, release)
               || excludeSubtitleKeywordMatch(subtitle, release)
               || excludeSubtitleExactMatch(subtitle, release);
    }

    private boolean excludeSubtitleHearingImpaired(Subtitle subtitle, Release release) {
        return SettingsControl.settings.optionSubtitleExcludeHearingImpaired && subtitle.hearingImpaired;
    }

    private boolean excludeSubtitleKeywordMatch(Subtitle subtitle, Release release) {
        return SettingsControl.settings.optionSubtitleKeywordMatch &&
            (keywordFilter.exclude(release, subtitle) || releaseGroupFilter.exclude(release, subtitle));
    }

    private boolean excludeSubtitleExactMatch(Subtitle subtitle, Release release) {
        return SettingsControl.settings.optionSubtitleExactMatch && exactNameFilter.exclude(release, subtitle);
    }
}
