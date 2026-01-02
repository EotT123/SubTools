package org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield;

import java.util.function.Function;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface MyTextFieldToObjectMapperIntf<T extends @Nullable Object, R extends MyTextFieldCommon<T, R>> {
    MyTextFieldOthersIntf<T, R> withToObjectMapper(Function<String, T> toObjectMapper);
}
