package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import javax.swing.*;
import java.io.Serial;

import net.miginfocom.swing.MigLayout;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.gui.extra.BoxModelProperties;
import org.lodder.subtools.multisubdownloader.gui.extra.MemoryFolderChooser;
import org.lodder.subtools.multisubdownloader.gui.extra.PanelCheckBox;
import org.lodder.subtools.multisubdownloader.gui.extra.TitlePanel;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield.MyTextFieldPath;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;

@NullMarked
public class SubtitleBackupPanel extends JPanel implements PreferencePanelIntf {

    @Serial private static final long serialVersionUID = 1L;

    private final LibrarySettings librarySettings;
    private final JCheckBox chkBackupSubtitle;
    private final MyTextFieldPath txtBackupSubtitlePath;
    private final JCheckBox chkBackupUseSourceFileName;

    public SubtitleBackupPanel(LibrarySettings librarySettings) {
        super(new MigLayout("insets 0, fillx, nogrid"));
        this.librarySettings = librarySettings;

        JPanel titlePanel = new TitlePanel(
            title:getText("PreferenceDialog.SubtitlesBackup"),
            margin:new BoxModelProperties(0),
            padding:new BoxModelProperties(left:20))
            .addToPanel(this, "span, growx");
        
        {
            this.txtBackupSubtitlePath = new MyTextFieldPath(true).columns(20);

            new PanelCheckBox(
                checkbox:this.chkBackupSubtitle = new JCheckBox(getText("PreferenceDialog.BackupSubtitles")),
                panelOnNewLine:true
                )
                .addToPanel(titlePanel, "span, wrap, growx")
                .addComponent("split 3, shrink", new JLabel(getText("PreferenceDialog.Location")))
                .addComponent("growx", txtBackupSubtitlePath)
                .addComponent("shrink",
                    new JButton(getText("App.Browse"))
                        .actionListener(_ -> MemoryFolderChooser.getInstance()
                            .selectDirectory(this, getText("PreferenceDialog.SubtitleBackupFolder"))
                            .ifPresent(txtBackupSubtitlePath::setObject)));

            chkBackupUseSourceFileName =
                new JCheckBox(getText("PreferenceDialog.IncludeSourceInFileName")).addTo(titlePanel);
        }

        loadPreferenceSettings();
    }

    public void loadPreferenceSettings() {
        chkBackupSubtitle.setSelected(librarySettings.backupSubtitle);
        txtBackupSubtitlePath.setObject(librarySettings.backupSubtitlePath);
        chkBackupUseSourceFileName.setSelected(librarySettings.backupUseWebsiteFileName);
    }

    public void savePreferenceSettings() {
        librarySettings.backupSubtitle = chkBackupSubtitle.isSelected();
        librarySettings.backupSubtitlePath = txtBackupSubtitlePath.getObject();
        librarySettings.backupUseWebsiteFileName = chkBackupUseSourceFileName.isSelected();
    }

    @Override
    public boolean hasValidSettings() {
        return txtBackupSubtitlePath.hasValidValue();
    }

}
