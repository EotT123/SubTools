package org.lodder.subtools.multisubdownloader.gui.workers;

import javax.swing.*;
import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.actions.RenameAction;
import org.lodder.subtools.multisubdownloader.gui.dialog.Cancelable;
import org.lodder.subtools.multisubdownloader.gui.extra.progress.StatusMessenger;
import org.lodder.subtools.multisubdownloader.gui.extra.table.CustomTable;
import org.lodder.subtools.multisubdownloader.gui.extra.table.VideoTableModel;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

@NullMarked
public class RenameWorker extends SwingWorker<Void, String> implements Cancelable {

    private final CustomTable table;
    private final Settings settings;
    private final Manager manager;
    private final UserInteractionHandler userInteractionHandler;

    public RenameWorker(CustomTable table, Settings settings, Manager manager,
        UserInteractionHandler userInteractionHandler) {
        this.table = table;
        this.settings = settings;
        this.manager = manager;
        this.userInteractionHandler = userInteractionHandler;
    }

    @Override
    protected Void doInBackground() {
        final VideoTableModel model = (VideoTableModel) table.getModel();

        model.executedSynchronized(() -> {
            List<Release> selectedShows = model.getSelectedShows();
            int selectedCount = selectedShows.size();
            int progress = 0;
            int k = 0;
            for (Release selectedShow : selectedShows) {
                k++;
                if (k > 0) {
                    progress = 100 * k / selectedCount;
                }
                if (progress == 0 && selectedCount > 1) {
                    progress = 1;
                }
                setProgress(progress);

                RenameAction renameAction = switch (selectedShow.videoType) {
                    case EPISODE -> new RenameAction(settings.episodeLibrarySettings, manager, userInteractionHandler);
                    case MOVIE -> new RenameAction(settings.movieLibrarySettings, manager, userInteractionHandler);
                };
                renameAction.rename(selectedShow.getPath().resolve(selectedShow.fileName), selectedShow);
                model.removeShow(selectedShow);
            }
        });
        return null;
    }

    @Override
    protected void process(List<String> data) {
        data.forEach(s -> StatusMessenger.instance.message(Messages.getText("MainWindow.RenamingFile", s)));
    }
}
