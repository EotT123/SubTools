package org.lodder.subtools.sublibrary.util.prompter;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.Messages;

@Deprecated
class PrompterBuilderCommon {

    private PrompterBuilderCommon() {
        // hide constructor
    }

    protected static <T> Optional<T> prompt(Prompter prompter, Function<String, T> toObjectMapper,
        @Nullable Predicate<String> validator=null, @Nullable Predicate<T> objValidator=null,
        @Nullable T defaultValue=null, @Nullable Supplier<T> defaultValueSupplier=null, String message,
        @Nullable String errorMessage=null) {
        try {
            String value = prompter.prompt(message + System.lineSeparator());
            if (StringUtils.isEmpty(value)) {
                if (defaultValue != null) {
                    return Optional.of(defaultValue);
                } else if (defaultValueSupplier != null) {
                    return Optional.ofNullable(defaultValueSupplier.get());
                } else {
                    return prompt(prompter, toObjectMapper, validator, objValidator, defaultValue, defaultValueSupplier,
                            message, errorMessage);
                }
            } else {
                if (validator != null && !validator.test(value)) {
                    prompter.showMessage(StringUtils.isNotBlank(errorMessage) ? errorMessage :
                            Messages.getText("Prompter.ValueIsNotValid"));
                    return prompt(prompter, toObjectMapper, validator, objValidator, defaultValue, defaultValueSupplier,
                            message, errorMessage);
                }
                T object = toObjectMapper.apply(value);
                if (objValidator != null && !objValidator.test(object)) {
                    prompter.showMessage(StringUtils.isNotBlank(errorMessage) ? errorMessage :
                            Messages.getText("Prompter.ValueIsNotValid"));
                    return prompt(prompter, toObjectMapper, validator, objValidator, defaultValue, defaultValueSupplier,
                            message, errorMessage);
                }
                return Optional.ofNullable(object);
            }
        } catch (PrompterException e) {
            throw new IllegalStateException(e);
        }
    }

}
