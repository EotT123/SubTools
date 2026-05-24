package org.lodder.subtools.multisubdownloader.gui.dialog;

import static org.lodder.subtools.multisubdownloader.Messages.*;
import static util.Utils.*;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import manifold.ext.props.rt.api.set;
import net.miginfocom.swing.MigLayout;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.actions.MoveAndRenameAction;
import org.lodder.subtools.multisubdownloader.gui.extra.BoxModelProperties;
import org.lodder.subtools.multisubdownloader.gui.extra.MemoryFolderChooser;
import org.lodder.subtools.multisubdownloader.gui.extra.TitlePanel;
import org.lodder.subtools.multisubdownloader.gui.extra.progress.StatusMessenger;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield.MyTextFieldPath;
import org.lodder.subtools.multisubdownloader.gui.panels.preference.EpisodeLibraryPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.preference.MovieLibraryPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.preference.VideoLibraryPanel;
import org.lodder.subtools.multisubdownloader.lib.ReleaseFactory;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.sublibrary.control.VideoPatterns;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

@NullMarked
public class RenameDialog extends MultiSubDialog implements PropertyChangeListener {

    @Serial private static final long serialVersionUID = 1L;

    private final VideoLibraryPanel pnlLibrary;
    private final MyTextFieldPath txtFolder;
    private final JCheckBox chkRecursive;

    private ProgressDialog progressDialog;

    public RenameDialog(@Nullable JFrame frame=null, VideoType videoType, String title,
        UserInteractionHandler userInteractionHandler) {
        super(frame, title, false);
        setResizable(false);
        setBounds(100, 100, 650, 680);
        contentPane.setLayout(new MigLayout("fill, nogrid", "[]", "[][]20:push[]"));

        new TitlePanel(
            title:getText("PreferenceDialog.Settings"),
            padding:new BoxModelProperties(left:20),
            fillContents:true)
            .addToPanel(contentPane, "span, grow, wrap")
            .addComponent("shrink", new JLabel(getText("PreferenceDialog.Location")))
            .addComponent("grow", this.txtFolder = new MyTextFieldPath(true).columns(20))
            .addComponent("shrink, wrap", new JButton(getText("App.Browse")).actionListener(
                () -> MemoryFolderChooser.getInstance()
                    .selectDirectory(contentPane,
                        getText("PreferenceDialog.SelectFolderForRenameReplace"))
                    .ifPresent(txtFolder::setObject)))
            .addComponent("wrap",
                this.chkRecursive = new JCheckBox(getText("RenameDialog.RecursiveSearch")));

        if (videoType == VideoType.EPISODE) {
            pnlLibrary = new EpisodeLibraryPanel(SettingsControl.settings.episodeLibrarySettings, true,
                userInteractionHandler).addTo(contentPane, "grow");
        } else {
            pnlLibrary = new MovieLibraryPanel(SettingsControl.settings.movieLibrarySettings, true,
                userInteractionHandler).addTo(contentPane, "grow");
        }

        new JPanel().layout(new FlowLayout(FlowLayout.RIGHT))
            .addTo(contentPane, BorderLayout.SOUTH)
            .addComponent(new JButton(getText("RenameDialog.Rename")).defaultButtonFor(getRootPane())
                .actionListener(() -> rename(videoType, userInteractionHandler))
                .actionCommand("Rename"))
            .addComponent(new JButton(getText("App.Cancel")).actionListener(() -> setVisible(false))
                .actionCommand("Cancel"));
    }

    private boolean hasValidSettings() {
        return pnlLibrary.hasValidSettings() && txtFolder.hasValidValue();
    }

    private void rename(VideoType videoType, UserInteractionHandler userInteractionHandler) {

        if (!hasValidSettings()) {
            JOptionPane.showMessageDialog(this, getText("PreferenceDialog.invalidInput"), "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        setVisible(false);
        pnlLibrary.savePreferenceSettings();
        TypedRenameWorker renameWorker =
            new TypedRenameWorker(txtFolder.getObject(), pnlLibrary.librarySettings, videoType,
                this.chkRecursive.isSelected(), userInteractionHandler);
        renameWorker.addPropertyChangeListener(this);
        renameWorker.releaseFactory = new ReleaseFactory();
        progressDialog = new ProgressDialog(renameWorker);
        progressDialog.setVisible(true);
        renameWorker.execute();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof TypedRenameWorker renameWorker) {
            if (renameWorker.isDone()) {
                progressDialog.setVisible(false);
            } else {
                final int progress = renameWorker.getProgress();
                progressDialog.updateProgress(progress);
                StatusMessenger.instance.message(getText("RenameDialog.StatusRename"));
            }
        }
    }

    @NullMarked
    private static class TypedRenameWorker extends SwingWorker<Void, String> implements Cancelable {

        private final UserInteractionHandler userInteractionHandler;
        private final Path dir;
        private final VideoType videoType;
        private final Set<String> extensions;
        private final boolean isRecursive;
        private final MoveAndRenameAction moveAndRenameAction;
        @set ReleaseFactory releaseFactory;

        public TypedRenameWorker(Path dir, LibrarySettings librarySettings, VideoType videoType, boolean isRecursive,
            UserInteractionHandler userInteractionHandler) {
            this.userInteractionHandler = userInteractionHandler;
            this.extensions = Streams.concat(VideoPatterns.EXTENSIONS.stream(), Stream.of("srt"))
                .collect(Collectors.toUnmodifiableSet());
            this.dir = dir;
            this.videoType = videoType;
            this.isRecursive = isRecursive;
            this.moveAndRenameAction = new MoveAndRenameAction(librarySettings, userInteractionHandler);
        }

        @Override
        protected Void doInBackground() throws IOException {
            rename(dir);
            return null;
        }

        private void rename(Path dir) throws IOException {
            dir.list().forEachEx(file -> {
                if (file.isRegularFile()) {
                    if (!file.fileNameContainsIgnoreCase("sample") && extensions.contains(file.getExtension())) {
                        ifNotNullDo(releaseFactory.createRelease(file, userInteractionHandler),
                            release -> {
                                publish(release.fileNameOrName);
                                if (release.isOfType(videoType)) {
                                    moveAndRenameAction.moveAndRename(file, release);
                                }
                            });
                    }
                } else if (isRecursive && file.isDirectory()) {
                    rename(file);
                }
            });
        }

        @Override
        protected void process(List<String> data) {
            data.forEach(s -> StatusMessenger.instance.message(getText("MainWindow.RenamingFile", s)));
        }
    }

}
