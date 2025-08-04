package org.lodder.subtools.multisubdownloader.settings.model.structure;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.multisubdownloader.Messages;

public enum FolderStructureTag implements StructureTag {

    SEPARATOR("%SEPARATOR%", Messages.getText("StructureBuilderDialog.SystemDependentSeparator"));

    @val @override String label;
    @val @override String description;

    FolderStructureTag(String label, String description) {
        this.label = label;
        this.description = description;
    }
}
