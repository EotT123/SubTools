package org.lodder.subtools.multisubdownloader.gui.panel.preference;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

@NullMarked
public final class MovieLibraryPanel extends VideoLibraryPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    public MovieLibraryPanel(LibrarySettings libSettings, boolean renameMode,
        UserInteractionHandler userInteractionHandler) {
        super(libSettings, VideoType.MOVIE, renameMode, userInteractionHandler);
        repaint();
    }
}
