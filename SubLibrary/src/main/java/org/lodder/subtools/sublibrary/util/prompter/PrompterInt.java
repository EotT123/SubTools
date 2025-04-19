package org.lodder.subtools.sublibrary.util.prompter;

import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.jspecify.annotations.Nullable;

public class PrompterInt {

    public static final Function<String, Integer> TO_OBJECT_MAPPER = Integer::parseInt;
    public static final Predicate<String> VALIDATOR = v -> {
        try {
            Integer.parseInt(v);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    };

    public final Predicate<Integer> objValidator;
    private final Integer defaultValue;
    private final Supplier<Integer> defaultValueSupplier;
    private final String message;
    private final String errorMessage;

    public PrompterInt(
        @Nullable IntPredicate validator=_ -> true,
        @Nullable Integer defaultValue=null,
        @Nullable IntSupplier defaultValueSupplier=null,
        String message,
        @Nullable String errorMessage=null) {
        this.objValidator = validator == null ? null : validator::test;
        this.defaultValue = defaultValue;
        this.defaultValueSupplier = defaultValueSupplier == null ? null : defaultValueSupplier::getAsInt;
        this.message = message;
        this.errorMessage = errorMessage;
    }

    public int prompt(Prompter prompter) {
        return PrompterBuilderCommon.prompt(prompter, TO_OBJECT_MAPPER, VALIDATOR, objValidator,
            defaultValue, defaultValueSupplier, message, errorMessage).get();
    }
}
