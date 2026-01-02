package org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield;

import java.util.function.Function;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface MyTextFieldToStringMapperIntf<T extends @Nullable Object, R extends MyTextFieldCommon<T, R>> {
    MyTextFieldToObjectMapperIntf<T, R> withToStringMapper(Function<T, String> toStringMapper);
}
