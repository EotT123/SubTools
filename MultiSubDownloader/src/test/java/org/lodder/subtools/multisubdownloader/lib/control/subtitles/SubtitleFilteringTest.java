package org.lodder.subtools.multisubdownloader.lib.control.subtitles;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.model.ReleaseWithoutPath;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.TvReleaseWithoutPath;
import org.mockito.MockedStatic;

class SubtitleFilteringTest {

    @Test
    void testExcludeImpairedHearingFiltering() {
        Subtitle subtitle1 = createSubtitle("", "", true, "");
        Subtitle subtitle2 = createSubtitle("", "", false, "");
        Subtitle subtitle3 = createSubtitle("", "", true, "");

        executeWithSettings(false, false, true, () -> {
            assertThatFilter(new SubtitleFiltering())
                .appliedOnSubtitles(subtitle1, subtitle2, subtitle3)
                .forRelease(createRelease("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.mkv", "DIMENSION"))
                .matchesSubtitles(subtitle2);
        });
    }

    @Test
    void testKeywordMatchFilter() {
        ReleaseWithoutPath release = createRelease("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.mkv", "DIMENSION");
        Subtitle subtitle1 = createSubtitle("Criminal.Minds.S10E12.HDTV.XviD-AFG.srt", "AFG", false, "");
        Subtitle subtitle2 = createSubtitle("criminal.minds.1012.hdtv-lol.srt", "lol", false, "");
        Subtitle subtitle3 =
            createSubtitle("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.srt", "DIMENSION", true, "");
        Subtitle subtitle4 =
            createSubtitle("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.srt", "DIMENSION", false, "");
        Subtitle subtitle5 = createSubtitle("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.srt", "DIMENSION", false,
            "720p HDTV X264");
        Subtitle subtitle6 =
            createSubtitle("Criminal.Minds.S10E12.Anonymous.1080p.WEB-DL.DD5.1.H.264-CtrlHD", "CtrlHD", false, "");

        // only keyword
        executeWithSettings(true, false, false, () -> {
            assertThatFilter(new SubtitleFiltering())
                .appliedOnSubtitles(subtitle1, subtitle2, subtitle3, subtitle4, subtitle5, subtitle6)
                .forRelease(release)
                .matchesSubtitles(subtitle3, subtitle4, subtitle5);
        });

        // keyword and exclude hearing impaired
        executeWithSettings(true, false, true, () -> {
            assertThatFilter(new SubtitleFiltering())
                .appliedOnSubtitles(subtitle1, subtitle2, subtitle3, subtitle4, subtitle5, subtitle6)
                .forRelease(release)
                .matchesSubtitles(subtitle4, subtitle5);
        });
    }

    @Test
    void testExactMatchFilter() {
        ReleaseWithoutPath release = createRelease("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.mkv", "DIMENSION");
        Subtitle subtitle1 = createSubtitle("Criminal.Minds.S10E12.HDTV.XviD-AFG.srt", "AFG", false, "");
        Subtitle subtitle2 = createSubtitle("criminal.minds.1012.hdtv-lol.srt", "lol", false, "");
        Subtitle subtitle3 =
            createSubtitle("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.srt", "DIMENSION", true, "");
        Subtitle subtitle4 =
            createSubtitle("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.srt", "DIMENSION", false, "");
        Subtitle subtitle5 =
            createSubtitle("Criminal.Minds.S10E12.Anonymous.1080p.WEB-DL.DD5.1.H.264-CtrlHD", "CtrlHD", false, "");

        // only exact match
        executeWithSettings(false, true, false, () -> {
            assertThatFilter(new SubtitleFiltering())
                .appliedOnSubtitles(subtitle1, subtitle2, subtitle3, subtitle4, subtitle5)
                .forRelease(release)
                .matchesSubtitles(subtitle3, subtitle4);
        });

        // exact match and exclude hearing impaired
        executeWithSettings(false, true, true, () -> {
            assertThatFilter(new SubtitleFiltering())
                .appliedOnSubtitles(subtitle1, subtitle2, subtitle3, subtitle4, subtitle5)
                .forRelease(release)
                .matchesSubtitles(subtitle4);
        });
    }

    @Test
    void testExactMatchAndKeywordMatchFilter() {
        ReleaseWithoutPath release =
            createRelease("Criminal.Minds.S10E12.Anonymous.720p.HDTV.X264-DIMENSION.mkv", "DIMENSION");
        Subtitle subtitle1 = createSubtitle("Criminal.Minds.S10E12.HDTV.XviD-AFG.srt", "AFG", false, "");
        Subtitle subtitle2 = createSubtitle("criminal.minds.1012.hdtv-lol.srt", "lol", false, "");
        Subtitle subtitle3 =
            createSubtitle("Criminal.Minds.S10E12.Anonymous.720p.HDTV.X264-DIMENSION.srt", "DIMENSION", true, "");
        Subtitle subtitle4 =
            createSubtitle("Criminal.Minds.S10E12.Anonymous.720p.HDTV.X264-DIMENSION.srt", "DIMENSION", false, "");
        Subtitle subtitle5 =
            createSubtitle("Criminal.Minds.S10E12.720p.HDTV.X264-DIMENSION.srt", "DIMENSION", false, "");
        Subtitle subtitle6 =
            createSubtitle("Criminal.Minds.S10E12.Anonymous.1080p.WEB-DL.DD5.1.H.264-CtrlHD", "CtrlHD", false, "");

        // only exact match
        executeWithSettings(true, true, false, () -> {
            assertThatFilter(new SubtitleFiltering())
                .appliedOnSubtitles(subtitle1, subtitle2, subtitle3, subtitle4, subtitle5, subtitle6)
                .forRelease(release)
                .matchesSubtitles(subtitle3, subtitle4);
        });

        // exact match and exclude hearing impaired
        executeWithSettings(true, true, true, () -> {
            assertThatFilter(new SubtitleFiltering())
                .appliedOnSubtitles(subtitle1, subtitle2, subtitle3, subtitle4, subtitle5, subtitle6)
                .forRelease(release)
                .matchesSubtitles(subtitle4);
        });
    }

    private void executeWithSettings(boolean keyword, boolean exact, boolean excludeHearingImpaired,
        Runnable runnable) {
        try (MockedStatic<SettingsControl> settingsControlMockedStatic = mockStatic(SettingsControl.class)) {
            Settings settings = mock(Settings.class);
            settingsControlMockedStatic.when(SettingsControl::getSettings).thenReturn(settings);
            when(settings.optionSubtitleExactMatch).thenReturn(exact);
            when(settings.optionSubtitleKeywordMatch).thenReturn(keyword);
            when(settings.optionSubtitleExcludeHearingImpaired).thenReturn(excludeHearingImpaired);
            runnable.run();
        }
    }

    private ReleaseWithoutPath createRelease(String filename, String releaseGroup) {
        ReleaseWithoutPath release = mock(TvReleaseWithoutPath.class);

        when(release.fileNameOrName).thenReturn(filename);
        when(release.releaseGroup).thenReturn(releaseGroup);

        return release;
    }

    private Subtitle createSubtitle(String filename, String releaseGroup, boolean excludeHearing, String quality) {
        Subtitle subtitle = mock(Subtitle.class);

        when(subtitle.fileName).thenReturn(filename);
        when(subtitle.releaseGroup).thenReturn(releaseGroup);
        when(subtitle.quality).thenReturn(quality);
        when(subtitle.hearingImpaired).thenReturn(excludeHearing);

        return subtitle;
    }


    private TestSetupSubtitlesIntf assertThatFilter(SubtitleFiltering filter) {
        return new TestSetupFiltering().assertThatFilter(filter);
    }

    @NullMarked
    private interface TestSetupSubtitlesIntf {
        TestSetupReleaseIntf appliedOnSubtitles(Subtitle... subtitles);
    }


    @NullMarked
    private interface TestSetupReleaseIntf {
        TestSetupMatchesIntf forRelease(ReleaseWithoutPath release);
    }

    @NullMarked
    private interface TestSetupMatchesIntf {
        void matchesSubtitles(Subtitle... subtitles);
    }

    @NullMarked
    private static class TestSetupFiltering
        implements TestSetupSubtitlesIntf, TestSetupReleaseIntf, TestSetupMatchesIntf {
        private @Nullable SubtitleFiltering filter;
        private @Nullable List<Subtitle> subtitles;
        private @Nullable ReleaseWithoutPath release;

        public TestSetupFiltering assertThatFilter(SubtitleFiltering filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public TestSetupFiltering appliedOnSubtitles(Subtitle... subtitles) {
            this.subtitles = subtitles.stream().toList();
            return this;
        }

        @Override
        public TestSetupFiltering forRelease(ReleaseWithoutPath release) {
            this.release = release;
            return this;
        }

        @Override
        public void matchesSubtitles(Subtitle... subtitles) {
            List<Subtitle> filteredSubtitles =
                this.subtitles.stream().filter(subtitle -> filter.useSubtitle(subtitle, release)).toList();
            assertThat(filteredSubtitles)
                .withFailMessage("Expected the filtered subtitles to contain exactly %s, but found %s".formatted(
                    subtitles.stream().map(Subtitle::getFileName).toList(),
                    filteredSubtitles.stream().map(Subtitle::getFileName).toList()))
                .containsExactlyInAnyOrder(subtitles);
        }
    }
}
