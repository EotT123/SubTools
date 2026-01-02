package org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield;

import static util.Utils.*;

import java.io.Serial;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class MyTextFieldInteger extends MyTextFieldCommon<@Nullable Integer, MyTextFieldInteger> {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final Function<@Nullable Integer, @Nullable String> TO_STRING_MAPPER =
        i -> ifNotNull(i, String::valueOf);
    private static final Function<@Nullable String, @Nullable Integer> TO_OBJECT_MAPPER =
        s -> ifNotNull(s, Integer::parseInt);
    public static final Predicate<String> INT_VERIFIER = text -> {
        try {
            if (StringUtils.isBlank(text)) {
                return true;
            }
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    };

    private MyTextFieldInteger() {

    }

    public static MyTextFieldOthersIntf<@Nullable Integer, MyTextFieldInteger> builder() {
        return new MyTextFieldInteger()
            .withToStringMapper(TO_STRING_MAPPER)
            .withToObjectMapper(TO_OBJECT_MAPPER)
            .withValueVerifier(INT_VERIFIER);
    }
}
