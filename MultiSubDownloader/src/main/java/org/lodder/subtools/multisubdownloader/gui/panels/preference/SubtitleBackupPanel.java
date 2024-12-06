package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import javax.swing.*;
import java.io.Serial;

import net.miginfocom.swing.MigLayout;
import org.lodder.subtools.multisubdownloader.Messages;
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

        JPanel titelPanel = TitlePanel.title(Messages.getString("PreferenceDialog.SubtitlesBackup"))
                .margin(0)
                .padding(0)
                .paddingLeft(20)
                .addTo(this, "span, growx");

        {
            this.txtBackupSubtitlePath = MyTextFieldPath.builder().requireValue().build().columns(20);

            PanelCheckBox.checkbox(
                            this.chkBackupSubtitle = new JCheckBox(Messages.getString("PreferenceDialog" +
                                    ".BackupSubtitles")))
                    .panelOnNewLine()
                    .addTo(titelPanel, "span, wrap, growx")
                    .addComponent("split 3, shrink", new JLabel(Messages.getString("PreferenceDialog.Location")))
                    .addComponent("growx", txtBackupSubtitlePath)
                    .addComponent("shrink", new JButton(Messages.getString("App.Browse")).actionListener(
                            l -> MemoryFolderChooser.getInstance()
                                    .selectDirectory(this, Messages.getString("PreferenceDialog.SubtitleBackupFolder"))
                                    .ifPresent(txtBackupSubtitlePath::setObject)));

            chkBackupUseSourceFileName =
                    new JCheckBox(Messages.getString("PreferenceDialog.IncludeSourceInFileName")).addTo(titelPanel);
        }

        loadPreferenceSettings();
    }

    public void loadPreferenceSettings() {
        chkBackupSubtitle.setSelected(librarySettings.libraryBackupSubtitle);
        txtBackupSubtitlePath.setObject(librarySettings.libraryBackupSubtitlePath);
        chkBackupUseSourceFileName.setSelected(librarySettings.libraryBackupUseWebsiteFileName);
    }

    public void savePreferenceSettings() {
        librarySettings.libraryBackupSubtitle = chkBackupSubtitle.isSelected();
        librarySettings.libraryBackupSubtitlePath = txtBackupSubtitlePath.getObject();
        librarySettings.libraryBackupUseWebsiteFileName = chkBackupUseSourceFileName.isSelected();
    }

    @Override
    public boolean hasValidSettings() {
        return txtBackupSubtitlePath.hasValidValue();
    }

}
