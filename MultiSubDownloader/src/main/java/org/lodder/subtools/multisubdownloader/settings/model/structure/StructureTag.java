package org.lodder.subtools.multisubdownloader.settings.model.structure;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface StructureTag {

    @val String label;
    @val String description;

}
