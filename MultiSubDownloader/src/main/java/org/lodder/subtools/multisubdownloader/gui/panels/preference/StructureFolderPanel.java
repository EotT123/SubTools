package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import javax.swing.*;
import java.io.Serial;
import java.util.function.Function;

import net.miginfocom.swing.MigLayout;
import org.lodder.subtools.multisubdownloader.gui.dialog.StructureBuilderDialog;
import org.lodder.subtools.multisubdownloader.gui.extra.MemoryFolderChooser;
import org.lodder.subtools.multisubdownloader.gui.extra.PanelCheckBox;
import org.lodder.subtools.multisubdownloader.gui.extra.TitlePanel;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield.MyTextFieldPath;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield.MyTextFieldString;
import org.lodder.subtools.multisubdownloader.lib.library.PathLibraryBuilder;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

public class StructureFolderPanel extends JPanel implements PreferencePanelIntf {

    @Serial
    private static final long serialVersionUID = 3476596236588408382L;

    private final LibrarySettings librarySettings;
    private final MyTextFieldPath txtLibraryFolder;
    private final MyTextFieldString txtFolderStructure;
    private final JCheckBox chkRemoveEmptyFolder;
    private final JCheckBox chkReplaceSpace;
    private final JComboBox<Character> cbxReplaceSpaceChar;

    public StructureFolderPanel(LibrarySettings librarySettings, VideoType videoType, Manager manager,
        UserInteractionHandler userInteractionHandler) {
        super(new MigLayout("insets 0, fill, nogrid"));
        this.librarySettings = librarySettings;

        JPanel titlePanel = TitlePanel.title(getText("PreferenceDialog.MoveToLibrary"))
            .margin(0).padding(0).marginLeft(20).paddingLeft(20).useGrid()
            .panelColumnConstraints("[shrink][grow][shrink]").addTo(this, "span, grow");

        new JLabel(getText("PreferenceDialog.Location")).addTo(titlePanel, "shrink");
        this.txtLibraryFolder =
            MyTextFieldPath.builder().requireValue().build().columns(20).addTo(titlePanel, "grow");
        new JButton(getText("App.Browse"))
            .actionListener(() -> MemoryFolderChooser.getInstance()
                .selectDirectory(getRootPane(), getText("PreferenceDialog.LibraryFolder"))
                .ifPresent(txtLibraryFolder::setObject))
            .addTo(titlePanel, "shrink, wrap");

        new JLabel(getText("StructureBuilderDialog.Structure")).addTo(titlePanel, "shrink");
        this.txtFolderStructure =
            MyTextFieldString.builder().requireValue().build().columns(20).disabled().addTo(titlePanel, "grow");
        JButton btnStructure = new JButton(getText("StructureBuilderDialog.Structure"))
            .actionListener(() -> {
                StructureBuilderDialog sDialog = new StructureBuilderDialog(null,
                    getText("PreferenceDialog.StructureBuilderTitle"),
                    true, videoType, StructureBuilderDialog.StructureType.FOLDER, manager,
                    userInteractionHandler, getLibraryStructureBuilder());
                String value = sDialog.showDialog(txtFolderStructure.getText());
                if (!"".equals(value)) {
                    txtFolderStructure.setText(value);
                }
            })
            .disabled()
            .addTo(titlePanel, "shrink, wrap");

        this.chkRemoveEmptyFolder = new JCheckBox(getText("PreferenceDialog.RemoveEmptyFolders"))
            .addTo(titlePanel, "span, wrap");

        PanelCheckBox.checkbox(this.chkReplaceSpace =
                new JCheckBox(getText("PreferenceDialog.ReplaceSpaceWith")))
            .panelOnSameLine().addTo(titlePanel, "span")
            .addComponent(this.cbxReplaceSpaceChar = JComboBox.create('-', '.', '_'));

        // behaviour
        txtLibraryFolder.withValidityChangedCallback(txtFolderStructure::setEnabled, btnStructure::setEnabled);

        loadPreferenceSettings();
    }

    private Function<String, PathLibraryBuilder> getLibraryStructureBuilder() {
        return structure -> PathLibraryBuilder.builder()
            .structure(structure)
            .replaceSpace(chkReplaceSpace.isSelected())
            .replacingSpaceChar(cbxReplaceSpaceChar.getSelectedValue())
            .useTvdbName(false)
            .tvdbAdapter(null)
            .libraryFolder(txtLibraryFolder.getObject())
            .move(true)
            .build();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        txtLibraryFolder.refreshState();
        txtFolderStructure.refreshState();
    }

    public void loadPreferenceSettings() {
        txtLibraryFolder.setObject(librarySettings.folder);
        txtFolderStructure.setText(librarySettings.folderStructure);
        chkRemoveEmptyFolder.setSelected(librarySettings.removeEmptyFolders);
        chkReplaceSpace.setSelected(librarySettings.folderReplaceSpace);
        cbxReplaceSpaceChar.setSelectedItem(librarySettings.folderReplacingSpaceChar);
    }

    public void savePreferenceSettings() {
        librarySettings.folder = txtLibraryFolder.getObject();
        librarySettings.folderStructure = txtFolderStructure.getText();
        librarySettings.removeEmptyFolders = chkRemoveEmptyFolder.isSelected();
        librarySettings.folderReplaceSpace = chkReplaceSpace.isSelected();
        librarySettings.folderReplacingSpaceChar = cbxReplaceSpaceChar.getSelectedValue();
    }

    @Override
    public boolean hasValidSettings() {
        return !isEnabled() || (txtLibraryFolder.hasValidValue() && txtFolderStructure.hasValidValue());
    }
}
