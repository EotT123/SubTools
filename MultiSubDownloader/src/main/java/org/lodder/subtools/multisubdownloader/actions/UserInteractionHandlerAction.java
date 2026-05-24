package org.lodder.subtools.multisubdownloader.actions;

import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.lib.control.subtitles.sorting.SubtitleComparator;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class UserInteractionHandlerAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInteractionHandlerAction.class);

    private final UserInteractionHandler userInteractionHandler;

    public UserInteractionHandlerAction(UserInteractionHandler userInteractionHandler) {
        this.userInteractionHandler = userInteractionHandler;
    }

    /**
     * @param release release
     * @param subtitleSelectionDialog subtitleSelectionDialog
     * @return integer which subtitle is selected for downloading
     */
    public List<Subtitle> subtitleSelection(Release release, boolean subtitleSelectionDialog) {
        return this.subtitleSelection(release, subtitleSelectionDialog, false);
    }

    /**
     * @param release release
     * @param subtitleSelectionDialog subtitleSelectionDialog
     * @param dryRun dryRun
     * @return integer which subtitle is selected for downloading
     */
    public List<Subtitle> subtitleSelection(Release release, final boolean subtitleSelectionDialog,
        final boolean dryRun) {

        // Sort subtitles by score
        List<Subtitle> subs = release.matchingSubs.stream().sorted(new SubtitleComparator()).toList();
        if (dryRun) {
            if (!subs.isEmpty()) {
                userInteractionHandler.dryRunOutput(release);
            }
        } else {
            if (!subs.isEmpty()) {
                LOGGER.debug("determineWhatSubtitleDownload for videoFile: [{}] # found subs: [{}]",
                    release.fileNameOrName, subs.size());
                if (SettingsControl.settings.optionsAlwaysConfirm) {
                    return userInteractionHandler.selectSubtitles(release);
                } else if (subs.size() == 1 && subs.first.subtitleMatchType == SubtitleMatchType.EXACT) {
                    LOGGER.debug("determineWhatSubtitleDownload: Exact Match");
                    return List.of(subs.first);
                } else if (subs.size() > 1) {
                    LOGGER.debug("determineWhatSubtitleDownload: Multiple subs detected");

                    // Automatic selection
                    List<Subtitle> shortlist = userInteractionHandler.getAutomaticSelection(subs);
                    shortlist.forEach(release::addMatchingSub);
                    if (shortlist.isEmpty()) {
                        // nothing match the minimum automatic selection value
                        return List.of();
                    } else if (shortlist.size() == 1) {
                        // automatic selection results in 1 result
                        return List.of(subs.first);
                    }

                    // still more than 1 subtitle, let the user decide!
                    if (subtitleSelectionDialog) {
                        LOGGER.debug("determineWhatSubtitleDownload: Select subtitle with dialog");
                        return userInteractionHandler.selectSubtitles(release);
                    } else {
                        LOGGER.info("Multiple subs detected for: [{}] Unhandleable for CMD! switch to GUI or use " +
                            "'--selection' as switch in de CMD", release.fileNameOrName);
                    }
                } else {
                    LOGGER.debug("determineWhatSubtitleDownload: only one sub taking it!!!!");
                    return List.of(subs.first);
                }
            }
            LOGGER.debug("determineWhatSubtitleDownload: No subs found for  [{}]", release.fileNameOrName);
        }
        return List.of();
    }
}
