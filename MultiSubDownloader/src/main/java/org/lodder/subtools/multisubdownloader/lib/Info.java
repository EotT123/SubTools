package org.lodder.subtools.multisubdownloader.lib;

import java.util.function.Consumer;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("java:S106")
@NullMarked
public class Info {

    private static final Logger LOGGER = LoggerFactory.getLogger(Info.class);

    private Info() {
        // Hide Utility Class Constructor
    }

    public static void subtitleSources(boolean isCli) {
        display(isCli, "----- Subtitle Providers ------");
        Settings settings = SettingsControl.settings;
        for (SubtitleSource source : SubtitleSource.values()) {
            boolean enabled = switch (source) {
                case ADDIC7ED -> settings.serieSourceAddic7ed;
                case LOCAL -> settings.serieSourceLocal;
                case OPENSUBTITLES -> settings.serieSourceOpensubtitles;
                case PODNAPISI -> settings.serieSourcePodnapisi;
                case TVSUBTITLES -> settings.serieSourceTvSubtitles;
                case SUBSCENE -> settings.serieSourceSubscene;
                case SUBDL -> settings.serieSourceSubdl;
            };
            display(isCli, " - provider : " + source + " enabled: " + enabled);
        }
        display(isCli, "-----------------------------");
    }

    public static void subtitleFiltering(boolean isCli) {
        Settings settings = SettingsControl.settings;
        display(isCli, "----- Subtitle Filtering ------");
        display(isCli, " - OptionSubtitleExactMatch: " + settings.optionSubtitleExactMatch);
        display(isCli, " - OptionSubtitleKeywordMatch: " + settings.optionSubtitleKeywordMatch);
        display(isCli, " - OptionSubtitleExcludeHearingImpaired: " + settings.optionSubtitleExcludeHearingImpaired);
        display(isCli, "-------------------------------");
    }

    public static void downloadOptions(boolean isCli) {
        display(isCli, "----- Download Options ------");
        display(isCli, " - OptionsAlwaysConfirm : " + SettingsControl.settings.optionsAlwaysConfirm);
        display(isCli, "-----------------------------");
    }

    private static void display(boolean isCli, String value) {
        Consumer<String> outputter = isCli ? System.out::println : LOGGER::info;
        outputter.accept(value);
    }
}
