package org.lodder.subtools.sublibrary.util.prompter;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.jspecify.annotations.Nullable;

@Deprecated
public class PrompterString {

    public final Predicate<String> validator;
    private final String defaultValue;
    private final Supplier<String> defaultValueSupplier;
    private final String message;
    private final String errorMessage;

    public PrompterString(
        @Nullable Predicate<String> validator=StringUtils::isNotBlank,
        @Nullable String defaultValue=null,
        @Nullable Supplier<String> defaultValueSupplier=null,
        String message,
        @Nullable String errorMessage=null) {
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.defaultValueSupplier = defaultValueSupplier;
        this.message = message;
        this.errorMessage = errorMessage;
    }

    public Optional<String> prompt(Prompter prompter) {
        return PrompterBuilderCommon.prompt(prompter, Function.identity(), validator, null,
            defaultValue, defaultValueSupplier, message, errorMessage);
    }
}
