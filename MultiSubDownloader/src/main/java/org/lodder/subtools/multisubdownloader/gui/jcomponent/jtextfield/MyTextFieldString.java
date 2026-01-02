package org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield;

import java.io.Serial;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class MyTextFieldString extends MyTextFieldCommon<String, MyTextFieldString> {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final Function<String, String> TO_STRING_MAPPER = Function.identity();
    private static final Function<String, String> TO_OBJECT_MAPPER = Function.identity();
    public static final Predicate<String> VERIFIER = _ -> true;

    public MyTextFieldString(
        boolean requireValue=false,
        Function<@Nullable String, @Nullable String> toStringMapper=TO_STRING_MAPPER,
        Function<@Nullable String, @Nullable String> toObjectMapper=TO_OBJECT_MAPPER,
        Predicate<String> valueVerifier=VERIFIER,
        @Nullable Consumer<String> valueChangedCallbackListener=null) {
        super(requireValue, toStringMapper, toObjectMapper, valueVerifier, valueChangedCallbackListener);
    }
}
