package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import static manifold.ext.props.rt.api.PropOption.*;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.Serial;

import manifold.ext.props.rt.api.get;
import org.lodder.subtools.multisubdownloader.Messages;

public abstract class StructurePanel<T extends StructurePanel<T>> extends JPanel implements PreferencePanelIntf {

    @Serial
    private static final long serialVersionUID = 7507970016496546514L;

    @get(Protected) JButton btnBuildStructure;
    @get(Protected) JCheckBox chkReplaceSpace;
    @get(Protected) JComboBox<String> cbxReplaceSpaceChar;

    StructurePanel() {
        this.btnBuildStructure = new JButton(Messages.getText("StructureBuilderDialog.Structure"));

        this.cbxReplaceSpaceChar = JComboBox.create("-", ".", "_");

        this.chkReplaceSpace = new JCheckBox(Messages.getText("PreferenceDialog.ReplaceSpaceWith"))
                .addCheckedChangeListener(cbxReplaceSpaceChar::setEnabled);
    }

    @SuppressWarnings("unchecked")
    public T addBuildStructureAction(ActionListener buildStructureAction) {
        btnBuildStructure.addActionListener(buildStructureAction);
        return (T) this;
    }

    public String getReplaceSpaceChar() {
        return this.cbxReplaceSpaceChar.getSelectedValue();
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
