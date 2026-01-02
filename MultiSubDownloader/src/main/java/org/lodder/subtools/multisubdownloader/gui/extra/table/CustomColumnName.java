package org.lodder.subtools.multisubdownloader.gui.extra.table;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface CustomColumnName {

    @val String columnName;
    @val boolean editable;
    @val Class<?> clazz;
}
