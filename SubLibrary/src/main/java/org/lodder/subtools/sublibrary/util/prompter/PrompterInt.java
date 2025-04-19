package org.lodder.subtools.sublibrary.util.prompter;

import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.jspecify.annotations.Nullable;

@Deprecated
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
