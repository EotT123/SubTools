package org.lodder.subtools.multisubdownloader.gui.actions.search;

import static util.Utils.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.GUI;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.actions.FileListAction;
import org.lodder.subtools.multisubdownloader.exceptions.SearchSetupException;
import org.lodder.subtools.multisubdownloader.gui.extra.table.VideoTableModel;
import org.lodder.subtools.multisubdownloader.gui.panels.SearchFileInputPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.SearchPanel;
import org.lodder.subtools.multisubdownloader.lib.ReleaseFactory;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.model.ReleaseWithPath;
import org.lodder.subtools.sublibrary.model.Subtitle;

@NullMarked
public final class FileGuiSearchAction extends GuiSearchAction<SearchFileInputPanel, ReleaseWithPath> {

    private final FileListAction filelistAction;

    public FileGuiSearchAction(GUI mainWindow, SearchPanel<SearchFileInputPanel> searchPanel,
        ReleaseFactory releaseFactory) {
        super(mainWindow, searchPanel, releaseFactory);
        this.filelistAction = new FileListAction();
    }

    @Override
    protected void validate() throws SearchSetupException {
        String path = getInputPanel().getIncomingPath();
        if (path.isEmpty() && !SettingsControl.settings.hasDefaultFolders()) {
            throw new SearchSetupException(Messages.getText("App.NoFolderSelected"));
        }
    }

    @Override
    public void onFound(ReleaseWithPath release, List<Subtitle> subtitles) {
        VideoTableModel model = (VideoTableModel) this.searchPanel.resultPanel.getTable().getModel();

        List<Subtitle> filteredSubtitles =
            subtitles.stream().filter(subtitle -> filtering.useSubtitle(subtitle, release)).toList();
        filteredSubtitles.forEach(release::addMatchingSub);

        model.addRow(release);
        mainWindow.repaint();

        /* Let GuiSearchAction also make some decisions */
        super.onFound(release, filteredSubtitles);
    }

    @Override
    protected List<ReleaseWithPath> createReleases() {
        SearchFileInputPanel inputPanel = getInputPanel();
        String filePath = inputPanel.getIncomingPath();
        Language language = inputPanel.getSelectedLanguage();
        boolean recursive = inputPanel.isRecursiveSelected();
        boolean overwriteExistingSubtitles = inputPanel.isForceOverwrite();

        VideoTableModel model = (VideoTableModel) this.searchPanel.resultPanel.getTable().getModel();
        model.clearTable();

        /* get a list of video files */
        List<Path> files = getFiles(filePath, language, recursive, overwriteExistingSubtitles);

        /* create a list of releases from video files */
        return createReleases(files);
    }

    private List<ReleaseWithPath> createReleases(List<Path> files) {
        /* parse every video file */
        List<ReleaseWithPath> releases = new ArrayList<>();

        int total = files.size();
        int index = 0;
        int progress = 0;

        this.indexingProgressListener.progress(progress);

        for (Path file : files) {
            index++;
            progress = (int) Math.floor((float) index / total * 100);

            /* Tell progressListener which file we are processing */
            this.indexingProgressListener.progress(file.getFileName().toString());

            ifNotNullDo(releaseFactory.createRelease(file, userInteractionHandler), releases::add);

            /* Update progressListener */
            this.indexingProgressListener.progress(progress);
        }

        return releases;
    }

    private List<Path> getFiles(String filePath, Language language, boolean recursive,
        boolean overwriteExistingSubtitles) {
        /* Get a list of selected directories */
        List<Path> dirs = !filePath.isEmpty() ? List.of(Path.of(filePath)) : SettingsControl.settings.defaultFolders;

        /* Scan directories for video files */
        /* Tell Action where to send progressUpdates */
        this.filelistAction.indexingProgressListener = this.indexingProgressListener;

        /* Start the getFileListing Action */
        return dirs.stream()
            .flatMap(dir -> this.filelistAction.getFileListing(dir, recursive, language, overwriteExistingSubtitles)
                .stream())
            .toList();
    }
}
