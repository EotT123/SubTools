package org.lodder.subtools.multisubdownloader.lib;

import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.model.Subtitle.SubtitleSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Info {

	private static final Logger LOGGER = LoggerFactory.getLogger(Info.class);

	public static void subtitleSources(Settings settings, boolean isCli) {
		if (isCli) {
			System.out.println("----- Subtitle Providers ------");
		} else {
			LOGGER.info("----- Subtitle Providers ------");
		}
		for (SubtitleSource source : SubtitleSource.values()) {
			boolean enabled = false;
			switch (source) {
				case ADDIC7ED:
					enabled = settings.isSerieSourceAddic7ed();
					break;
				case LOCAL:
					enabled = settings.isSerieSourceLocal();
					break;
				case OPENSUBTITLES:
					enabled = settings.isSerieSourceOpensubtitles();
					break;
				case PODNAPISI:
					enabled = settings.isSerieSourcePodnapisi();
					break;
				case SUBSMAX:
					enabled = settings.isSerieSourceSubsMax();
					break;
				case TVSUBTITLES:
					enabled = settings.isSerieSourceTvSubtitles();
					break;
				default:
					break;

			}
			if (isCli) {
				System.out.println(" - provider : " + source.toString() + " enabled: " + enabled);
			} else {
				LOGGER.info(" - provider : " + source.toString() + " enabled: " + enabled);
			}
		}
		if (isCli) {
			System.out.println("-----------------------------");
		} else {
			LOGGER.info("-----------------------------");
		}
	}

	public static void subtitleFiltering(Settings settings, boolean isClie) {
		if (isClie) {
			System.out.println("----- Subtitle Filtering ------");
			System.out.println(" - OptionSubtitleExactMatch : " + settings.isOptionSubtitleExactMatch());
			System.out.println(" - OptionSubtitleKeywordMatch : "
					+ settings.isOptionSubtitleKeywordMatch());
			System.out.println(" - OptionSubtitleExcludeHearingImpaired : "
					+ settings.isOptionSubtitleExcludeHearingImpaired());
			System.out.println("-------------------------------");
		} else {
			LOGGER.info("----- Subtitle Filtering ------");
			LOGGER.info(" - OptionSubtitleExactMatch: {} ", settings.isOptionSubtitleExactMatch());
			LOGGER.info(" - OptionSubtitleKeywordMatch: {} ", settings.isOptionSubtitleKeywordMatch());
			LOGGER.info(" - OptionSubtitleExcludeHearingImpaired: {} ",
					settings.isOptionSubtitleExcludeHearingImpaired());
			LOGGER.info("-------------------------------");
		}

	}

	public static void downloadOptions(Settings settings, boolean isCli) {
		if (isCli) {
			System.out.println("----- Download Options ------");
			System.out.println(" - OptionsAlwaysConfirm : " + settings.isOptionsAlwaysConfirm());
			System.out.println("-----------------------------");
		} else {
			LOGGER.info("----- Download Options ------");
			LOGGER.info(" - OptionsAlwaysConfirm : " + settings.isOptionsAlwaysConfirm());
			LOGGER.info("-----------------------------");
		}

	}
}
