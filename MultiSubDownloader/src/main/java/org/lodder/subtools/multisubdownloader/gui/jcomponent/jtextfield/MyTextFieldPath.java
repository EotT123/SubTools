package org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield;

import static util.Utils.*;

import java.io.Serial;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class MyTextFieldPath extends MyTextFieldCommon<@Nullable Path, MyTextFieldPath> {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final Function<@Nullable Path, @Nullable String> TO_STRING_MAPPER =
        path -> ifNotNull(path, p -> p.toAbsolutePath().toString());
    private static final Function<@Nullable String, @Nullable Path> TO_OBJECT_MAPPER = s -> ifNotNull(s, Path::of);
    public static final Predicate<String> ABSOLUTE_PATH_VERIFIER = text -> {
        try {
            return StringUtils.isBlank(text) || Path.of(text).isAbsolute();
        } catch (InvalidPathException e) {
            return false;
        }
    };

    private MyTextFieldPath() {

    }

    public static MyTextFieldOthersIntf<@Nullable Path, MyTextFieldPath> builder() {
        return new MyTextFieldPath()
            .withToStringMapper(TO_STRING_MAPPER)
            .withToObjectMapper(TO_OBJECT_MAPPER)
            .withValueVerifier(ABSOLUTE_PATH_VERIFIER);
    }
}
