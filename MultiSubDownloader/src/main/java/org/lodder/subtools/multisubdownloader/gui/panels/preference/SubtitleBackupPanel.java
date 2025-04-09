package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import javax.swing.*;
import java.io.Serial;

import net.miginfocom.swing.MigLayout;
import org.lodder.subtools.multisubdownloader.gui.extra.MemoryFolderChooser;
import org.lodder.subtools.multisubdownloader.gui.extra.PanelCheckBox;
import org.lodder.subtools.multisubdownloader.gui.extra.TitlePanel;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield.MyTextFieldPath;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;

public class SubtitleBackupPanel extends JPanel implements PreferencePanelIntf {

    @Serial private static final long serialVersionUID = -1498846730946617177L;

    private final LibrarySettings librarySettings;
    private final JCheckBox chkBackupSubtitle;
    private final MyTextFieldPath txtBackupSubtitlePath;
    private final JCheckBox chkBackupUseSourceFileName;

    public SubtitleBackupPanel(LibrarySettings librarySettings) {
        super(new MigLayout("insets 0, fillx, nogrid"));
        this.librarySettings = librarySettings;

        JPanel titlePanel = TitlePanel.title(getText("PreferenceDialog.SubtitlesBackup"))
            .margin(0).padding(0).paddingLeft(20).addTo(this, "span, growx");

        {
            this.txtBackupSubtitlePath = MyTextFieldPath.builder().requireValue().build().columns(20);

            PanelCheckBox.checkbox(this.chkBackupSubtitle = new JCheckBox(getText("PreferenceDialog.BackupSubtitles")))
                .panelOnNewLine()
                .addTo(titlePanel, "span, wrap, growx")
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
