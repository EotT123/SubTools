package org.lodder.subtools.sublibrary.util.prompter;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.jspecify.annotations.Nullable;

public class PrompterBoolean {

    public final Function<String, Boolean> toObjectMapper;
    public final Predicate<String> validator;
    private final Boolean defaultValue;
    private final Supplier<Boolean> defaultValueSupplier;
    private final String message;
    private final String errorMessage;

    public PrompterBoolean(
        Predicate<String> toObjectMapper="y"::equalsIgnoreCase,
        @Nullable Predicate<String> validator=v -> "y".equalsIgnoreCase(v) || "n".equalsIgnoreCase(v),
        @Nullable Boolean defaultValue=null,
        @Nullable BooleanSupplier defaultValueSupplier=null,
        String message,
        @Nullable String errorMessage=null) {
        this.toObjectMapper = toObjectMapper::test;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.defaultValueSupplier = defaultValueSupplier == null ? null : defaultValueSupplier::getAsBoolean;
        this.message = message;
        this.errorMessage = errorMessage;
    }

    public boolean prompt(Prompter prompter) {
        return PrompterBuilderCommon.prompt(prompter, toObjectMapper, validator, null, defaultValue,
            defaultValueSupplier, message, errorMessage).get();
    }

//
//    private PrompterBoolean() {
//        // util class
//    }
//
//    protected static boolean getValue(Prompter prompter) {
//        return new ValueBuilder().prompt(prompter);
//    }
//
//    protected static ValueBuilderOtherMapperIntf getValue() {
//        return new ValueBuilder();
//    }
//
//    public static ValueBuilderOtherMapperIntf defaultValue(boolean defaultValue) {
//        return new ValueBuilder().defaultValue(defaultValue);
//    }
//
//    public static ValueBuilderOtherMapperIntf defaultValueSupplier(BooleanSupplier defaultValueSupplier) {
//        return new ValueBuilder().defaultValueSupplier(defaultValueSupplier);
//    }
//
//    public static ValueBuilderOtherMapperIntf message(String message, Object... replacements) {
//        return new ValueBuilder().message(message, replacements);
//    }
//
//    public interface ValueBuilderOtherMapperIntf {
//
//        ValueBuilderOtherMapperIntf defaultValue(boolean defaultValue);
//
//        ValueBuilderOtherMapperIntf defaultValueSupplier(BooleanSupplier defaultValueSupplier);
//
//        ValueBuilderOtherMapperIntf message(String message, Object... replacements);
//
//        ValueBuilderOtherMapperIntf errorMessage(String errorMessage, Object... replacements);
//
//        boolean prompt(Prompter prompter);
//    }
//
//    // ------- \\
//    // Builder \\
//    // ------- \\
//
//    @Setter
//    @Accessors(fluent = true, chain = true)
//    public static class ValueBuilder implements ValueBuilderOtherMapperIntf {
//        public static final Predicate<String> TO_OBJECT_MAPPER = "y"::equalsIgnoreCase;
//        public static final Predicate<String> VALIDATOR = v -> "y".equalsIgnoreCase(v) || "n".equalsIgnoreCase(v);
//        private Boolean defaultValue;
//        private BooleanSupplier defaultValueSupplier;
//        private String message;
//        private String errorMessage;
//
//        private ValueBuilder() {
//            // hide constructor
//        }
//
//        @Override
//        public ValueBuilder defaultValue(boolean defaultValue) {
//            this.defaultValue = defaultValue;
//            return this;
//        }
//
//        @Override
//        public ValueBuilder message(String message, Object... replacements) {
//            this.message = message.formatted(replacements);
//            return this;
//        }
//
//        @Override
//        public ValueBuilder errorMessage(String errorMessage, Object... replacements) {
//            this.errorMessage = errorMessage.formatted(replacements);
//            return this;
//        }
//
//        @Override
//        public boolean prompt(Prompter prompter) {
//            return PrompterBuilderCommon.prompt(prompter, TO_OBJECT_MAPPER::test, VALIDATOR, null, defaultValue,
//                    defaultValueSupplier == null ? null : defaultValueSupplier::getAsBoolean, message, errorMessage).get();
//        }
//    }
}
