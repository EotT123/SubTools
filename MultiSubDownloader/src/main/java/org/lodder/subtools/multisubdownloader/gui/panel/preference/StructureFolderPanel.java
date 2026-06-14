package org.lodder.subtools.multisubdownloader.gui.panel.preference;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import javax.swing.*;
import java.io.Serial;
import java.util.function.Function;

import net.miginfocom.swing.MigLayout;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.gui.dialog.StructureBuilderDialog;
import org.lodder.subtools.multisubdownloader.gui.extra.BoxModelProperties;
import org.lodder.subtools.multisubdownloader.gui.extra.MemoryFolderChooser;
import org.lodder.subtools.multisubdownloader.gui.extra.PanelCheckBox;
import org.lodder.subtools.multisubdownloader.gui.extra.TitlePanel;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield.MyTextFieldPath;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield.MyTextFieldString;
import org.lodder.subtools.multisubdownloader.lib.library.PathLibraryBuilder;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

@NullMarked
public class StructureFolderPanel extends JPanel implements PreferencePanelIntf {

    @Serial
    private static final long serialVersionUID = 1L;

    private final LibrarySettings librarySettings;
    private final MyTextFieldPath txtLibraryFolder;
    private final MyTextFieldString txtFolderStructure;
    private final JCheckBox chkRemoveEmptyFolder;
    private final JCheckBox chkReplaceSpace;
    private final JComboBox<Character> cbxReplaceSpaceChar;

    public StructureFolderPanel(LibrarySettings librarySettings, VideoType videoType,
        UserInteractionHandler userInteractionHandler) {
        super(new MigLayout("insets 0, fill, nogrid"));
        this.librarySettings = librarySettings;

        JPanel titlePanel = new TitlePanel(
            title:getText("PreferenceDialog.MoveToLibrary"),
            margin:new BoxModelProperties(left:20),
            padding:new BoxModelProperties(left:20),
            useGrid:true,
            panelColumnConstraints:"[shrink][grow][shrink]")
            .addToPanel(this, "span, grow");

        new JLabel(getText("PreferenceDialog.Location")).addTo(titlePanel, "shrink");
        this.txtLibraryFolder = new MyTextFieldPath(true).columns(20).addTo(titlePanel, "grow");
        new JButton(getText("App.Browse"))
            .actionListener(() -> MemoryFolderChooser.getInstance()
                .selectDirectory(getRootPane(), getText("PreferenceDialog.LibraryFolder"))
                .ifPresent(txtLibraryFolder::setObject))
            .addTo(titlePanel, "shrink, wrap");

        new JLabel(getText("StructureBuilderDialog.Structure")).addTo(titlePanel, "shrink");
        this.txtFolderStructure = new MyTextFieldString(true).columns(20).disabled().addTo(titlePanel, "grow");
        JButton btnStructure = new JButton(getText("StructureBuilderDialog.Structure"))
            .actionListener(() -> {
                StructureBuilderDialog sDialog = new StructureBuilderDialog(null,
                    getText("PreferenceDialog.StructureBuilderTitle"),
                    true, videoType, StructureBuilderDialog.StructureType.FOLDER, userInteractionHandler,
                    getLibraryStructureBuilder());
                String value = sDialog.showDialog(txtFolderStructure.getText());
                if (!value.isEmpty()) {
                    txtFolderStructure.setText(value);
                }
            })
            .disabled()
            .addTo(titlePanel, "shrink, wrap");

        this.chkRemoveEmptyFolder = new JCheckBox(getText("PreferenceDialog.RemoveEmptyFolders"))
            .addTo(titlePanel, "span, wrap");

        new PanelCheckBox(
            checkbox:this.chkReplaceSpace = new JCheckBox(getText("PreferenceDialog.ReplaceSpaceWith")),
            panelOnNewLine:false
            )
            .addToPanel(titlePanel, "span")
            .addComponent(this.cbxReplaceSpaceChar = JComboBox.create('-', '.', '_'));

        // behaviour
        txtLibraryFolder.addValidityChangedCallbackListeners(txtFolderStructure::setEnabled)
            .addValidityChangedCallbackListeners(btnStructure::setEnabled);

        loadPreferenceSettings();
    }

    private Function<String, PathLibraryBuilder> getLibraryStructureBuilder() {
        return structure -> new PathLibraryBuilder(
            structure:structure,
            replaceSpace:chkReplaceSpace.isSelected(),
            replacingSpaceChar:cbxReplaceSpaceChar.getSelectedValue(),
            libraryFolder:txtLibraryFolder.getObject(),
            move:true);
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
        chkReplaceSpace.setSelected(librarySettings.folderReplacingSpaceChar != null);
        cbxReplaceSpaceChar.setSelectedItem(librarySettings.folderReplacingSpaceChar);
    }

    public void savePreferenceSettings() {
        librarySettings.folder = txtLibraryFolder.getObject();
        librarySettings.folderStructure = txtFolderStructure.getText();
        librarySettings.removeEmptyFolders = chkRemoveEmptyFolder.isSelected();
        librarySettings.folderReplacingSpaceChar = cbxReplaceSpaceChar.getSelectedValue();
    }

    @Override
    public boolean hasValidSettings() {
        return !isEnabled() || (txtLibraryFolder.hasValidValue() && txtFolderStructure.hasValidValue());
    }
}
