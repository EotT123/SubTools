package org.lodder.subtools.multisubdownloader;

import java.util.ArrayList;
import java.util.List;

import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;

public interface UserInteractionHandler extends org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler {

    default List<Subtitle> getAutomaticSelection(List<Subtitle> subtitles) {
        final List<Subtitle> shortlist;

        if (getSettings().isOptionsMinAutomaticSelection()) {
            shortlist = subtitles.stream()
                    .filter(subtitle -> subtitle.getScore() >= getSettings().getOptionsMinAutomaticSelectionValue())
                    .toList();
        } else {
            shortlist = new ArrayList<>(subtitles);
        }

        if (getSettings().isOptionsDefaultSelection()) {
            List<Subtitle> defaultSelectionsFound = getSettings().getOptionsDefaultSelectionQualityList().stream()
                    .flatMap(q -> shortlist.stream().filter(subtitle -> q.isTypeForValue(subtitle.getQuality())))
                    .distinct().toList();

            if (!defaultSelectionsFound.isEmpty()) {
                return defaultSelectionsFound;
            }
        }
        return shortlist;
    }

    List<Subtitle> selectSubtitles(Release release);

    void dryRunOutput(Release release);

}
