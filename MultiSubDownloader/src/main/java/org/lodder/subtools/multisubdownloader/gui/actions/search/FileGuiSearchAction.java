package org.lodder.subtools.multisubdownloader.gui.actions.search;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.lodder.subtools.multisubdownloader.GUI;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.actions.ActionException;
import org.lodder.subtools.multisubdownloader.actions.FileListAction;
import org.lodder.subtools.multisubdownloader.exceptions.SearchSetupException;
import org.lodder.subtools.multisubdownloader.gui.extra.table.VideoTableModel;
import org.lodder.subtools.multisubdownloader.gui.panels.SearchFileInputPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.SearchPanel;
import org.lodder.subtools.multisubdownloader.lib.ReleaseFactory;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

public class FileGuiSearchAction extends GuiSearchAction<SearchFileInputPanel> {

    private final @NonNull FileListAction filelistAction;

    public interface FileGuiSearchActionBuilderManager {
        FileGuiSearchActionBuilderSubtitleProviderStore manager(Manager manager);
    }

    public interface FileGuiSearchActionBuilderSubtitleProviderStore {
        FileGuiSearchActionBuilderGUI subtitleProviderStore(SubtitleProviderStore subtitleProviderStore);
    }

    public interface FileGuiSearchActionBuilderGUI {
        FileGuiSearchActionBuilderSearchPanel mainwindow(GUI mainwindow);
    }

    public interface FileGuiSearchActionBuilderSearchPanel {
        FileGuiSearchActionBuilderReleaseFactory searchPanel(SearchPanel<SearchFileInputPanel> searchPanel);
    }

    public interface FileGuiSearchActionBuilderReleaseFactory {
        FileGuiSearchActionBuilderBuild releaseFactory(ReleaseFactory releaseFactory);
    }

    public interface FileGuiSearchActionBuilderBuild {
        FileGuiSearchAction build() throws SearchSetupException;
    }

    public static FileGuiSearchActionBuilderManager createWithSettings(Settings settings) {
        return new FileGuiSearchActionBuilder(settings);
    }

    @RequiredArgsConstructor
    @Setter
    @Accessors(chain = true, fluent = true)
    public static class FileGuiSearchActionBuilder
            implements FileGuiSearchActionBuilderBuild, FileGuiSearchActionBuilderReleaseFactory,
            FileGuiSearchActionBuilderSearchPanel, FileGuiSearchActionBuilderGUI,
            FileGuiSearchActionBuilderSubtitleProviderStore, FileGuiSearchActionBuilderManager {
        private final Settings settings;
        private Manager manager;
        private SubtitleProviderStore subtitleProviderStore;
        private GUI mainwindow;
        private SearchPanel<SearchFileInputPanel> searchPanel;
        private ReleaseFactory releaseFactory;

        @Override
        public FileGuiSearchAction build() throws SearchSetupException {
            return new FileGuiSearchAction(manager, settings, subtitleProviderStore, mainwindow, searchPanel, releaseFactory);
        }
    }

    private FileGuiSearchAction(Manager manager, Settings settings, SubtitleProviderStore subtitleProviderStore, GUI mainwindow,
            SearchPanel<SearchFileInputPanel> searchPanel, ReleaseFactory releaseFactory) throws SearchSetupException {
        super(manager, settings, subtitleProviderStore, mainwindow, searchPanel, releaseFactory);
        this.filelistAction = new FileListAction(settings);
    }

    @Override
    protected void validate() throws SearchSetupException {
        String path = getInputPanel().getIncomingPath();
        if ("".equals(path) && !this.getSettings().hasDefaultFolders()) {
            throw new SearchSetupException(Messages.getString("App.NoFolderSelected"));
        }
    }

    @Override
    public void onFound(Release release, List<Subtitle> subtitles) {
        VideoTableModel model = (VideoTableModel) this.getSearchPanel().getResultPanel().getTable().getModel();

        if (getFiltering() != null) {
            subtitles = getFiltering().getFiltered(subtitles, release);
        }
        subtitles.forEach(release::addMatchingSub);

        model.addRow(release);
        getMainwindow().repaint();

        /* Let GuiSearchAction also make some decisions */
        super.onFound(release, subtitles);
    }

    @Override
    protected List<Release> createReleases() throws ActionException {
        SearchFileInputPanel inputPanel = getInputPanel();
        String filePath = inputPanel.getIncomingPath();
        Language language = inputPanel.getSelectedLanguage();
        boolean recursive = inputPanel.isRecursiveSelected();
        boolean overwriteExistingSubtitles = inputPanel.isForceOverwrite();

        VideoTableModel model = (VideoTableModel) this.getSearchPanel().getResultPanel().getTable().getModel();
        model.clearTable();

        /* get a list of videofiles */
        List<Path> files = getFiles(filePath, language, recursive, overwriteExistingSubtitles);

        /* create a list of releases from videofiles */
        return createReleases(files);
    }

    private List<Release> createReleases(List<Path> files) throws ActionException {
        /* parse every videofile */
        List<Release> releases = new ArrayList<>();

        int total = files.size();
        int index = 0;
        int progress = 0;

        this.getIndexingProgressListener().progress(progress);

        for (Path file : files) {
            index++;
            progress = (int) Math.floor((float) index / total * 100);

            /* Tell progressListener which file we are processing */
            this.getIndexingProgressListener().progress(file.getFileName().toString());

            Release r = getReleaseFactory().createRelease(file, getUserInteractionHandler());
            if (r != null) {
                releases.add(r);
            }

            /* Update progressListener */
            this.getIndexingProgressListener().progress(progress);
        }

        return releases;
    }

    private List<Path> getFiles(String filePath, Language language, boolean recursive, boolean overwriteExistingSubtitles) {
        /* Get a list of selected directories */
        List<Path> dirs = new ArrayList<>();
        if (!filePath.isEmpty()) {
            dirs.add(Path.of(filePath));
        } else {
            dirs.addAll(this.getSettings().getDefaultFolders());
        }

        /* Scan directories for videofiles */
        /* Tell Action where to send progressUpdates */
        this.filelistAction.setIndexingProgressListener(this.getIndexingProgressListener());

        /* Start the getFileListing Action */
        return dirs.stream()
                .flatMap(dir -> this.filelistAction.getFileListing(dir, recursive, language, overwriteExistingSubtitles).stream())
                .toList();
    }
}
