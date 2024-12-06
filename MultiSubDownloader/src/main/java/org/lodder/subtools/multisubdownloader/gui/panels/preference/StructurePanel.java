package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import static manifold.ext.props.rt.api.PropOption.*;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.Serial;

import manifold.ext.props.rt.api.get;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jcombobox.MyComboBox;

public abstract class StructurePanel<T extends StructurePanel<T>> extends JPanel implements PreferencePanelIntf {

    @Serial
    private static final long serialVersionUID = 7507970016496546514L;

    @get(Protected) JButton btnBuildStructure;
    @get(Protected) JCheckBox chkReplaceSpace;
    @get(Protected) MyComboBox<String> cbxReplaceSpaceChar;

    StructurePanel() {
        this.btnBuildStructure = new JButton(Messages.getString("StructureBuilderDialog.Structure"));

        this.cbxReplaceSpaceChar = new MyComboBox<>(new String[]{ "-", ".", "_" });

        this.chkReplaceSpace = new JCheckBox(Messages.getString("PreferenceDialog.ReplaceSpaceWith"))
                .addCheckedChangeListener(cbxReplaceSpaceChar::setEnabled);
    }

    @SuppressWarnings("unchecked")
    public T addBuildStructureAction(ActionListener buildStructureAction) {
        btnBuildStructure.addActionListener(buildStructureAction);
        return (T) this;
    }

    public String getReplaceSpaceChar() {
        return this.cbxReplaceSpaceChar.getSelectedItem();
    }

    public void setReplaceSpaceChar(String s) {
        this.cbxReplaceSpaceChar.setSelectedItem(s);
    }

    public boolean isReplaceSpaceSelected() {
        return this.chkReplaceSpace.isSelected();
    }

    public void setReplaceSpaceSelected(boolean b) {
        this.chkReplaceSpace.setSelected(b);
    }

}
