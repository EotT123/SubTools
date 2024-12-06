package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import javax.swing.*;
import java.io.Serial;
import java.util.function.Function;

import net.miginfocom.swing.MigLayout;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.gui.dialog.StructureBuilderDialog;
import org.lodder.subtools.multisubdownloader.gui.extra.MemoryFolderChooser;
import org.lodder.subtools.multisubdownloader.gui.extra.PanelCheckBox;
import org.lodder.subtools.multisubdownloader.gui.extra.TitlePanel;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jcombobox.MyComboBox;
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
    private final MyComboBox<Character> cbxReplaceSpaceChar;

    public StructureFolderPanel(LibrarySettings librarySettings, VideoType videoType, Manager manager,
            UserInteractionHandler userInteractionHandler) {
        super(new MigLayout("insets 0, fill, nogrid"));
        this.librarySettings = librarySettings;

        JPanel titlePanel = TitlePanel.title(Messages.getString("PreferenceDialog.MoveToLibrary"))
                .margin(0).padding(0).marginLeft(20).paddingLeft(20).useGrid()
                .panelColumnConstraints("[shrink][grow][shrink]").addTo(this, "span, grow");

        {
            new JLabel(Messages.getString("PreferenceDialog.Location")).addTo(titlePanel, "shrink");
            this.txtLibraryFolder =
                    MyTextFieldPath.builder().requireValue().build().withColumns(20).addTo(titlePanel, "grow");
            new JButton(Messages.getString("App.Browse"))
                    .withActionListener(() -> MemoryFolderChooser.getInstance()
                            .selectDirectory(getRootPane(), Messages.getString("PreferenceDialog.LibraryFolder"))
                            .ifPresent(txtLibraryFolder::setObject))
                    .addTo(titlePanel, "shrink, wrap");

            new JLabel(Messages.getString("StructureBuilderDialog.Structure")).addTo(titlePanel, "shrink");
            this.txtFolderStructure = MyTextFieldString.builder()
                    .requireValue()
                    .build()
                    .withColumns(20)
                    .withDisabled()
                    .addTo(titlePanel, "grow");
            JButton btnStructure = new JButton(Messages.getString("StructureBuilderDialog.Structure"))
                    .withActionListener(() -> {
                        StructureBuilderDialog sDialog = new StructureBuilderDialog(null,
                                Messages.getString("PreferenceDialog.StructureBuilderTitle"),
                                true, videoType, StructureBuilderDialog.StructureType.FOLDER, manager,
                                userInteractionHandler, getLibraryStructureBuilder());
                        String value = sDialog.showDialog(txtFolderStructure.getText());
                        if (!"".equals(value)) {
                            txtFolderStructure.setText(value);
                        }

                    })
                    .withDisabled()
                    .addTo(titlePanel, "shrink, wrap");

            this.chkRemoveEmptyFolder = new JCheckBox(Messages.getString("PreferenceDialog.RemoveEmptyFolders"))
                    .addTo(titlePanel, "span, wrap");

            PanelCheckBox.checkbox(this.chkReplaceSpace =
                            new JCheckBox(Messages.getString("PreferenceDialog.ReplaceSpaceWith")))
                    .panelOnSameLine().addTo(titlePanel, "span")
                    .addComponent(this.cbxReplaceSpaceChar = MyComboBox.ofValues('-', '.', '_'));

            // behaviour
            txtLibraryFolder.withValidityChangedCallback(txtFolderStructure::setEnabled, btnStructure::setEnabled);
        }

        loadPreferenceSettings();
    }

    private Function<String, PathLibraryBuilder> getLibraryStructureBuilder() {
        return structure -> PathLibraryBuilder.builder()
                .structure(structure)
                .replaceSpace(chkReplaceSpace.isSelected())
                .replacingSpaceChar(cbxReplaceSpaceChar.getSelectedItem())
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
        txtLibraryFolder.setObject(librarySettings.libraryFolder);
        txtFolderStructure.setText(librarySettings.libraryFolderStructure);
        chkRemoveEmptyFolder.setSelected(librarySettings.libraryRemoveEmptyFolders);
        chkReplaceSpace.setSelected(librarySettings.libraryFolderReplaceSpace);
        cbxReplaceSpaceChar.setSelectedItem(librarySettings.libraryFolderReplacingSpaceChar);
    }

    public void savePreferenceSettings() {
        librarySettings.libraryFolder = txtLibraryFolder.getObject();
        librarySettings.libraryFolderStructure = txtFolderStructure.getText();
        librarySettings.libraryRemoveEmptyFolders = chkRemoveEmptyFolder.isSelected();
        librarySettings.libraryFolderReplaceSpace = chkReplaceSpace.isSelected();
        librarySettings.libraryFolderReplacingSpaceChar = cbxReplaceSpaceChar.getSelectedItem();
    }

    @Override
    public boolean hasValidSettings() {
        return !isEnabled() || (txtLibraryFolder.hasValidValue() && txtFolderStructure.hasValidValue());
    }
}
