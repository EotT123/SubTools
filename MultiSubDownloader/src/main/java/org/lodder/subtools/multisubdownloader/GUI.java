package org.lodder.subtools.multisubdownloader;

import static org.lodder.subtools.multisubdownloader.Messages.*;
import static org.lodder.subtools.multisubdownloader.gui.extra.table.SearchColumnName.*;
import static util.Utils.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.gui.Menu;
import org.lodder.subtools.multisubdownloader.gui.actions.search.FileGuiSearchAction;
import org.lodder.subtools.multisubdownloader.gui.actions.search.TextGuiSearchAction;
import org.lodder.subtools.multisubdownloader.gui.dialog.Cancelable;
import org.lodder.subtools.multisubdownloader.gui.dialog.MappingEpisodeNameDialog;
import org.lodder.subtools.multisubdownloader.gui.dialog.PreferenceDialog;
import org.lodder.subtools.multisubdownloader.gui.dialog.ProgressDialog;
import org.lodder.subtools.multisubdownloader.gui.dialog.RenameDialog;
import org.lodder.subtools.multisubdownloader.gui.dialog.progress.fileindexer.IndexingProgressDialog;
import org.lodder.subtools.multisubdownloader.gui.dialog.progress.search.SearchProgressDialog;
import org.lodder.subtools.multisubdownloader.gui.extra.MemoryFolderChooser;
import org.lodder.subtools.multisubdownloader.gui.extra.PopupListener;
import org.lodder.subtools.multisubdownloader.gui.extra.progress.StatusLabel;
import org.lodder.subtools.multisubdownloader.gui.extra.progress.StatusMessenger;
import org.lodder.subtools.multisubdownloader.gui.extra.table.CustomTable;
import org.lodder.subtools.multisubdownloader.gui.extra.table.SearchColumnName;
import org.lodder.subtools.multisubdownloader.gui.extra.table.VideoTableModel;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jpopupmenu.MyPopupMenu;
import org.lodder.subtools.multisubdownloader.gui.panels.LoggingPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.ResultPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.SearchFileInputPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.SearchPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.SearchTextInputPanel;
import org.lodder.subtools.multisubdownloader.gui.workers.DownloadWorker;
import org.lodder.subtools.multisubdownloader.gui.workers.RenameWorker;
import org.lodder.subtools.multisubdownloader.lib.ReleaseFactory;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.settings.model.ScreenSettings;
import org.lodder.subtools.multisubdownloader.util.ExportImport;
import org.lodder.subtools.multisubdownloader.util.PropertiesReader;
import org.lodder.subtools.multisubdownloader.util.PropertiesReader.PomProperty;
import org.lodder.subtools.sublibrary.ConfigProperties;
import org.lodder.subtools.sublibrary.ConfigProperties.Property;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.OsCheck;
import org.lodder.subtools.sublibrary.OsCheck.OSType;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.util.function.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class GUI extends JFrame implements PropertyChangeListener {

    @Serial private static final long serialVersionUID = 1L;
    private final UserInteractionHandlerGUI userInteractionHandler;
    private ProgressDialog progressDialog;
    private SearchPanel<SearchFileInputPanel> pnlSearchFile;
    private SearchPanel<SearchTextInputPanel> pnlSearchText;
    private SearchFileInputPanel pnlSearchFileInput;
    private Menu menuBar;
    private IndexingProgressDialog fileIndexerProgressDialog;

    private static final Logger LOGGER = LoggerFactory.getLogger(GUI.class);

    /**
     * Create the application.
     */
    public GUI() {
        this.userInteractionHandler = new UserInteractionHandlerGUI(SettingsControl.settings, this);
        setTitle(ConfigProperties.getProperty(Property.NAME));
        /*
         * setIconImage(Toolkit.getDefaultToolkit().getImage(
         * getClass().getResource("/resources/Bierdopje_bigger.png")));
         */
        initialize();
        restoreScreenSettings();
        pnlSearchFile.resultPanel.disableButtons();
        pnlSearchText.resultPanel.disableButtons();
        new Thread(() -> checkUpdate(false)).start();
        initPopupMenu();
    }

    public void redraw() {
        close();
        // setVisible(false);
        contentPane.removeAll();
        initialize();
    }

    private void checkUpdate(final boolean forceUpdateCheck) {
        UpdateAvailableGithub u = new UpdateAvailableGithub();
        String updateUrl = (forceUpdateCheck && u.isNewVersionAvailable()) ||
            (!forceUpdateCheck && u.shouldCheckForNewUpdate(SettingsControl.settings.updateCheckPeriod) &&
                u.isNewVersionAvailable()) ? u.getLatestDownloadUrl() : null;
        if (updateUrl != null) {
            final JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(800, 50));
            editorPane.setEditable(false);
            editorPane.setContentType("text/html");

            editorPane.setText("<html>" + getText("UpdateAppAvailable") + "!: </br><A HREF=" + updateUrl + ">" +
                updateUrl + "</a></html>");

            editorPane.addHyperlinkListener(hyperlinkEvent -> {
                if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED &&
                    Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(hyperlinkEvent.getURL().toURI());
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                }
            });
            JOptionPane.showMessageDialog(this, editorPane, ConfigProperties.getProperty(Property.NAME),
                JOptionPane.INFORMATION_MESSAGE);
        } else if (forceUpdateCheck) {
            JOptionPane.showMessageDialog(this, getText("MainWindow.NoUpdateAvailable"),
                ConfigProperties.getProperty(Property.NAME), JOptionPane.INFORMATION_MESSAGE);
        }

    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        MemoryFolderChooser.getInstance().memory = SettingsControl.settings.lastOutputDir;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        setBounds(100, 100, 925, 680);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{448, 0};
        gridBagLayout.rowHeights = new int[]{0, 125, 15, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gridBagLayout);

        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        GridBagConstraints gbcTabbedPane = new GridBagConstraints();
        gbcTabbedPane.insets = new Insets(0, 0, 5, 0);
        gbcTabbedPane.fill = GridBagConstraints.BOTH;
        gbcTabbedPane.gridx = 0;
        gbcTabbedPane.gridy = 0;
        contentPane.add(tabbedPane, gbcTabbedPane);

        createFileSearchPanel();
        tabbedPane.addTab(getText("MainWindow.SearchOnFile"), null, pnlSearchFile, null);

        createTextSearchPanel();
        tabbedPane.addTab(getText("MainWindow.SearchOnName"), null, pnlSearchText, null);

        LoggingPanel pnlLogging = new LoggingPanel();
        final GridBagConstraints gbcPnlLogging = new GridBagConstraints();
        gbcPnlLogging.fill = GridBagConstraints.BOTH;
        gbcPnlLogging.insets = new Insets(0, 0, 5, 0);
        gbcPnlLogging.gridx = 0;
        gbcPnlLogging.gridy = 1;
        contentPane.add(pnlLogging, gbcPnlLogging);

        StatusLabel lblStatus = new StatusLabel("");
        StatusMessenger.instance.addListener(lblStatus);
        final GridBagConstraints gbcLblStatus = new GridBagConstraints();
        gbcLblStatus.anchor = GridBagConstraints.SOUTHWEST;
        gbcLblStatus.gridx = 0;
        gbcLblStatus.gridy = 2;
        contentPane.add(lblStatus, gbcLblStatus);

        createMenu(pnlLogging);
        setJMenuBar(menuBar);
    }

    private void createMenu(LoggingPanel pnlLogging) {
        BiConsumer<SearchColumnName, Boolean> visibilityFunction =
            pnlSearchFile.resultPanel.getTable()::setColumnVisibility;
        BiConsumer<VideoType, String> showRenameDialog =
            (videoType, title) -> new RenameDialog(self(), videoType, title, userInteractionHandler).setVisible(true);
        ExportImport exportImport = new ExportImport(userInteractionHandler, this);
        menuBar = new Menu()
            .withShowOnlyFound(SettingsControl.settings.optionsShowOnlyFound)
            .withFileQuitAction(this::close)
            .withViewFilenameAction(() -> visibilityFunction.accept(FILENAME, menuBar.isViewFilenameSelected()))
            .withViewTitleAction(() -> visibilityFunction.accept(TITLE, menuBar.isViewTitleSelected()))
            .withViewSeasonAction(() -> visibilityFunction.accept(SEASON, menuBar.isViewSeasonSelected()))
            .withViewEpisodeAction(() -> visibilityFunction.accept(EPISODE, menuBar.isViewEpisodeSelected()))
            .withViewShowOnlyFoundAction(() -> {
                SettingsControl.settings.optionsShowOnlyFound = menuBar.isShowOnlyFound();
                ((VideoTableModel) pnlSearchFile.resultPanel.getTable().getModel())
                    .setShowOnlyFound(menuBar.isShowOnlyFound());
            })
            .withViewClearLogAction(() -> pnlLogging.setLogText(""))
            .withEditRenameTVAction(() -> showRenameDialog.accept(VideoType.EPISODE, getText("Menu.RenameSerie")))
            .withEditRenameMovieAction(() -> showRenameDialog.accept(VideoType.MOVIE, getText("Menu.RenameMovie")))
            .withEditPreferencesAction(
                () -> new PreferenceDialog(self(), userInteractionHandler).setVisible(true))
            .withTranslateShowNamesAction(this::showTranslateShowNames)
            .withExportTranslationsAction(() -> exportImport.exportSettings(ExportImport.SettingsType.SERIE_MAPPING))
            .withImportTranslationsAction(() -> exportImport.importSettings(ExportImport.SettingsType.SERIE_MAPPING))
            .withExportPreferencesAction(() -> exportImport.exportSettings(ExportImport.SettingsType.PREFERENCES))
            .withImportPreferencesAction(() -> exportImport.importSettings(ExportImport.SettingsType.PREFERENCES))
            .withCheckUpdateAction(() -> checkUpdate(true))
            .withAboutAction(this::showAbout);
    }

    private void createTextSearchPanel() {
        /* resolve the SubtitleProviderStore from the Container */
        ResultPanel resultPanel = new ResultPanel();
        SearchTextInputPanel pnlSearchTextInput = new SearchTextInputPanel();
        pnlSearchText = new SearchPanel<>(pnlSearchTextInput, resultPanel);
        pnlSearchTextInput.setSelectedLanguage(
            ifNullThen(SettingsControl.settings.subtitleLanguage, Language.DUTCH_FLEMISH));
        resultPanel.showSelectFoundSubtitlesButton();
        resultPanel.setTable(createSubtitleTable());
        resultPanel.setDownloadAction(_ -> downloadText());

        TextGuiSearchAction searchAction = new TextGuiSearchAction(this, pnlSearchText, new ReleaseFactory());
        pnlSearchTextInput.addSearchAction(searchAction);
    }

    private CustomTable createSubtitleTable() {
        CustomTable subtitleTable = new CustomTable();
        subtitleTable.setModel(VideoTableModel.getDefaultSubtitleTableModel());
        final RowSorter<TableModel> sorterSubtitle = new TableRowSorter<>(subtitleTable.getModel());
        subtitleTable.setRowSorter(sorterSubtitle);
        subtitleTable.hideColumn(SearchColumnName.OBJECT);
        return subtitleTable;
    }

    private void createFileSearchPanel() {
        ResultPanel resultPanel = new ResultPanel();
        pnlSearchFileInput = new SearchFileInputPanel();
        pnlSearchFileInput.setRecursiveSelected(SettingsControl.settings.optionRecursive);
        pnlSearchFileInput.setSelectedLanguage(
            ifNullThen(SettingsControl.settings.subtitleLanguage, Language.DUTCH_FLEMISH));
        pnlSearchFile = new SearchPanel<>(pnlSearchFileInput, resultPanel);

        resultPanel.setTable(createVideoTable());

        FileGuiSearchAction searchAction = new FileGuiSearchAction(this, pnlSearchFile, new ReleaseFactory());

        pnlSearchFileInput.addSelectFolderAction(_ -> selectIncomingFolder());
        pnlSearchFileInput.addSearchAction(searchAction);

        resultPanel.setDownloadAction(_ -> download());
        resultPanel.setMoveAction(_ -> {
            final int response = JOptionPane.showConfirmDialog(self(), getText("MainWindow.OnlyMoveToLibraryStructure"),
                getText("App.Confirm"), //$NON-NLS-2$
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                rename();
            }
        });
    }

    private CustomTable createVideoTable() {
        CustomTable customTable = new CustomTable();
        VideoTableModel videoTableModel = VideoTableModel.getDefaultVideoTableModel();
        customTable.setModel(videoTableModel);
        videoTableModel.setShowOnlyFound(SettingsControl.settings.optionsShowOnlyFound);
        videoTableModel.userInteractionHandler = userInteractionHandler;
        final RowSorter<TableModel> sorter = new TableRowSorter<>(customTable.getModel());
        customTable.setRowSorter(sorter);
        customTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        int columnId = customTable.getColumnIdByName(SearchColumnName.FOUND);
        customTable.getColumnModel().getColumn(columnId).setResizable(false);
        customTable.getColumnModel().getColumn(columnId).setPreferredWidth(100);
        customTable.getColumnModel().getColumn(columnId).setMaxWidth(100);
        columnId = customTable.getColumnIdByName(SearchColumnName.SELECT);
        customTable.getColumnModel().getColumn(columnId).setResizable(false);
        customTable.getColumnModel().getColumn(columnId).setPreferredWidth(85);
        customTable.getColumnModel().getColumn(columnId).setMaxWidth(85);
        customTable.hideColumn(SearchColumnName.OBJECT);
        customTable.hideColumn(SearchColumnName.SEASON);
        customTable.hideColumn(SearchColumnName.EPISODE);
        customTable.hideColumn(SearchColumnName.TITLE);
        return customTable;
    }

    private void restoreScreenSettings() {
        CustomTable customTable = pnlSearchFile.resultPanel.getTable();
        TriConsumer<SearchColumnName, Boolean, Consumer<Boolean>> visibilityConsumer =
            (searchColumn, hidden, setVisibleConsumer) -> {
                setVisibleConsumer.accept(!hidden);
                customTable.setColumnVisibility(searchColumn, !hidden);
            };

        ScreenSettings screenSettings = SettingsControl.settings.screenSettings;

        visibilityConsumer.accept(SearchColumnName.EPISODE, screenSettings.hideEpisode,
            menuBar::withViewEpisodeSelected);
        visibilityConsumer.accept(FILENAME, screenSettings.hideFilename, menuBar::withViewFileNameSelected);
        visibilityConsumer.accept(SearchColumnName.SEASON, screenSettings.hideSeason, menuBar::withViewSeasonSelected);
        visibilityConsumer.accept(SearchColumnName.TITLE, screenSettings.hideTitle, menuBar::withViewTitleSelected);
    }

    private void initPopupMenu() {
        MyPopupMenu popupMenu = new MyPopupMenu();
        JMenuItem menuItem = new JMenuItem(getText("App.Copy"));
        menuItem.addActionListener(_ -> {
            final CustomTable t = (CustomTable) popupMenu.getInvoker();
            final DefaultTableModel model = (DefaultTableModel) t.getModel();

            int col = t.columnAtPoint(popupMenu.clickLocation);
            int row = t.rowAtPoint(popupMenu.clickLocation);

            try {
                StringSelection selection = new StringSelection((String) model.getValueAt(row, col));
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            } catch (HeadlessException e) {
                LOGGER.error("initPopupMenu", e);
            }
        });
        popupMenu.add(menuItem);
        // add the listener to the jtable
        MouseListener popupListener = new PopupListener(popupMenu);
        // add the listener specifically to the header
        CustomTable customTable = pnlSearchFile.resultPanel.getTable();
        CustomTable subtitleTable = pnlSearchText.resultPanel.getTable();
        customTable.addMouseListener(popupListener);
        customTable.getTableHeader().addMouseListener(popupListener);
        subtitleTable.addMouseListener(popupListener);
        subtitleTable.getTableHeader().addMouseListener(popupListener);
    }

    protected void showTranslateShowNames() {
        final MappingEpisodeNameDialog tDialog = new MappingEpisodeNameDialog(this, userInteractionHandler);
        tDialog.setVisible(true);
    }

    private void showAbout() {
        String version = ConfigProperties.getProperty(Property.VERSION);
        String currentVersionText = getText("MainWindow.CurrentVersion");
        String buildTimestamp = PropertiesReader.getProperty(PomProperty.BUILD_TIMESTAMP);
        String text = "$currentVersionText: $version";
        if (version.contains("-SNAPSHOT")) {
            text += " ($buildTimestamp)";
        }
        JOptionPane.showConfirmDialog(this, text, ConfigProperties.getProperty(Property.NAME),
            JOptionPane.DEFAULT_OPTION);
    }

    protected void rename() {
        CustomTable customTable = pnlSearchFile.resultPanel.getTable();
        RenameWorker renameWorker = new RenameWorker(customTable, userInteractionHandler);
        renameWorker.addPropertyChangeListener(this);
        pnlSearchFile.resultPanel.enableButtons();
        progressDialog = new ProgressDialog(this, renameWorker);
        progressDialog.setVisible(true);
        renameWorker.execute();
    }

    private void download() {
        CustomTable customTable = pnlSearchFile.resultPanel.getTable();
        DownloadWorker downloadWorker = new DownloadWorker(customTable, this);
        downloadWorker.addPropertyChangeListener(this);
        pnlSearchFile.resultPanel.disableButtons();
        progressDialog = new ProgressDialog(this, downloadWorker);
        progressDialog.setVisible(true);
        downloadWorker.execute();
    }

    private void downloadText() {
        MemoryFolderChooser.getInstance()
            .selectDirectory(contentPane, getText("MainWindow.SelectFolder"))
            .ifPresent(folder -> {
                CustomTable subtitleTable = pnlSearchText.resultPanel.getTable();
                final VideoTableModel model = (VideoTableModel) subtitleTable.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    if ((Boolean) model.getValueAt(i, subtitleTable.getColumnIdByName(SearchColumnName.SELECT))) {
                        final Subtitle subtitle = (Subtitle) model.getValueAt(i,
                            subtitleTable.getColumnIdByName(SearchColumnName.OBJECT));
                        Function<AtomicInteger, String> filenameSupplier = _ -> {
                            String filename = "";
                            if (!subtitle.fileName.endsWith(".srt")) {
                                filename = subtitle.fileName + ".srt";
                            }
                            if (OsCheck.OPERATING_SYSTEM_TYPE == OSType.WINDOWS) {
                                filename = filename.removeIllegalWindowsChars();
                            }
                            return filename;
                        };
                        try {
                            subtitle.download(folder, filenameSupplier);
                        } catch (IOException e) {
                            LOGGER.error("downloadText", e);
                        }
                    }
                }
            });
    }

    protected GUI self() {
        return this;
    }

    public void showErrorMessage(String message) {
        JOptionPane.showConfirmDialog(this, message, ConfigProperties.getProperty(Property.NAME),
            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
    }

    private void selectIncomingFolder() {
        MemoryFolderChooser.getInstance()
            .selectDirectory(self(), getText("MainWindow.SelectFolder"))
            .map(Path::toAbsolutePath)
            .map(Path::toString)
            .ifPresent(pnlSearchFileInput::setIncomingPath);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof DownloadWorker downloadWorker) {
            if (downloadWorker.isDone()) {
                pnlSearchFile.resultPanel.enableButtons();
                progressDialog.setVisible(false);
            } else {
                final int progress = downloadWorker.getProgress();
                progressDialog.updateProgress(progress);
                StatusMessenger.instance.message(getText("MainWindow.StatusDownload"));
            }
        } else if (event.getSource() instanceof RenameWorker renameWorker) {
            if (renameWorker.isDone()) {
                pnlSearchFile.resultPanel.enableButtons();
                progressDialog.setVisible(false);
            } else {
                final int progress = renameWorker.getProgress();
                progressDialog.updateProgress(progress);
                StatusMessenger.instance.message(getText("MainWindow.StatusRename"));
            }
        }
    }

    private void close() {
        SettingsControl.settings.optionRecursive = pnlSearchFileInput.isRecursiveSelected();
        SettingsControl.settings.subtitleLanguage = pnlSearchFileInput.getSelectedLanguage();
        storeScreenSettings();
        SettingsControl.store();
    }

    private void storeScreenSettings() {
        CustomTable customTable = pnlSearchFile.resultPanel.getTable();
        SettingsControl.settings.screenSettings.hideEpisode = customTable.isHideColumn(SearchColumnName.EPISODE);
        SettingsControl.settings.screenSettings.hideFilename = customTable.isHideColumn(FILENAME);
        SettingsControl.settings.screenSettings.hideSeason = customTable.isHideColumn(SearchColumnName.SEASON);
        SettingsControl.settings.screenSettings.hideTitle = customTable.isHideColumn(SearchColumnName.TITLE);
    }

    public ProgressDialog setProgressDialog(Cancelable worker) {
        progressDialog = new ProgressDialog(this, worker);
        return progressDialog;
    }

    public void showProgressDialog() {
        this.progressDialog.setVisible(true);
    }

    public void hideProgressDialog() {
        this.progressDialog.setVisible(false);
    }

    public void setStatusMessage(String message) {
        StatusMessenger.instance.message(message);
    }

    public void updateProgressDialog(int progress) {
        progressDialog.updateProgress(progress);
    }

    public SearchProgressDialog createSearchProgressDialog(Cancelable searchAction) {
        return new SearchProgressDialog(this, searchAction);
    }

    public IndexingProgressDialog createFileIndexerProgressDialog(Cancelable searchAction) {
        fileIndexerProgressDialog = new IndexingProgressDialog(this, searchAction);
        return fileIndexerProgressDialog;
    }

    public void hideFileIndexerProgressDialog() {
        if (fileIndexerProgressDialog == null) {
            return;
        }
        fileIndexerProgressDialog.setVisible(false);
    }
}
