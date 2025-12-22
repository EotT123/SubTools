package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

@NullMarked
public final class EpisodeLibraryPanel extends VideoLibraryPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    public EpisodeLibraryPanel(LibrarySettings libSettings, Manager manager, boolean renameMode,
            UserInteractionHandler userInteractionHandler) {
        super(libSettings, VideoType.EPISODE, manager, renameMode, userInteractionHandler);
        repaint();
    }
}
