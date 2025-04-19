package extensions.org.codehaus.plexus.components.interactivity.Prompter;

import static com.pivovarit.gatherers.MoreGatherers.*;
import static java.lang.System.*;
import static org.lodder.subtools.multisubdownloader.Messages.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import dnl.utils.text.table.TextTable;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.jspecify.annotations.Nullable;

@Extension
@UtilityClass
public class PrompterExt {

    public static void show(@This Prompter prompter, String message, Object... replacements) {
        try {
            prompter.showMessage(message.formatted(replacements));
        } catch (PrompterException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void pressAnyKeyToContinue(@This Prompter prompter) {
        prompter.promptString(message:"Press any key to continue", defaultValue:"");
//        new PrompterString(message:"Press any key to continue", defaultValue:"").prompt(prompter);
    }

    public static Optional<String> promptString(@This Prompter prompter,
        @Nullable Predicate<String> validator=StringUtils::isNotBlank,
        @Nullable String defaultValue=null,
        @Nullable Supplier<String> defaultValueSupplier=null,
        String message,
        @Nullable String errorMessage=null) {

        return prompt(prompter, Function.identity(), validator, null, defaultValue, defaultValueSupplier, message,
            errorMessage);
    }

    public static Optional<Integer> promptInt(@This Prompter prompter,
        @Nullable IntPredicate validator=_ -> true,
        @Nullable Integer defaultValue=null,
        @Nullable IntSupplier defaultValueSupplier=null,
        String message,
        @Nullable String errorMessage=null) {

        Predicate<String> isIntValidator = v -> {
            try {
                Integer.parseInt(v);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        };
        return prompt(prompter, Integer::parseInt, isIntValidator, validator == null ? null : validator::test,
            defaultValue, defaultValueSupplier == null ? null : defaultValueSupplier::getAsInt, message, errorMessage);
    }

    public static Optional<Boolean> promptBoolean(@This Prompter prompter,
        Predicate<String> toObjectMapper="y"::equalsIgnoreCase,
        @Nullable Predicate<String> validator=v -> "y".equalsIgnoreCase(v) || "n".equalsIgnoreCase(v),
        @Nullable Boolean defaultValue=null,
        @Nullable BooleanSupplier defaultValueSupplier=null,
        String message,
        @Nullable String errorMessage=null) {

        return prompt(prompter, toObjectMapper::test, validator, null,
            defaultValue, defaultValueSupplier == null ? null : defaultValueSupplier::getAsBoolean, message,
            errorMessage);
    }

    public static <T> Optional<T> promptValue(@This Prompter prompter,
        Iterable<T> elements,
        Function<T, String> toStringMapper=String::valueOf,
        String message,
        boolean includeNull,
        T emptyValue=null,
        @Nullable TableDisplayer<T> tableDisplayer=null,
        @Nullable Comparator<T> sorter=null) {
        List<T> sortedElements =
            sorter == null ? elements.stream().toList() : elements.stream().sorted(sorter).toList();
        try {
            String value = promptFromListIndices(prompter, sortedElements, tableDisplayer, toStringMapper, message);
            if (StringUtils.isBlank(value) && includeNull) {
                return Optional.ofNullable(emptyValue);
            }
            int number = Integer.parseInt(value);
            if (number < 1 || number > sortedElements.size()) {
                prompter.show("The entered value isn't in the range [1, %s], try again.", sortedElements.size());
                return prompter.promptValue(elements, toStringMapper, message, includeNull, emptyValue,
                    tableDisplayer, sorter);
            }
            return Optional.ofNullable(sortedElements.get(number - 1));
        } catch (NumberFormatException e) {
            prompter.show("Enter a valid number, try again.");
            return prompter.promptValue(elements, toStringMapper, message, includeNull, emptyValue,
                tableDisplayer, sorter);
        }

    }

    public static <T> List<T> promptValues(@This Prompter prompter,
        Iterable<T> elements,
        Function<T, String> toStringMapper=String::valueOf,
        String message,
        boolean includeNull,
        @Nullable TableDisplayer<T> tableDisplayer=null,
        @Nullable Comparator<T> sorter=null) {

        List<T> sortedElements =
            sorter == null ? elements.stream().toList() : elements.stream().sorted(sorter).toList();
        try {
            String value = promptFromListIndices(prompter, sortedElements, tableDisplayer, toStringMapper, message);

            if (StringUtils.isBlank(value) && includeNull) {
                return List.of();
            }
            if (StringUtils.isBlank(value)) {
                prompter.show("Enter a valid value, try again.");
                return prompter.promptValues(elements, toStringMapper, message, includeNull, tableDisplayer, sorter);
            }
            int[] choices = value.split(",").stream().mapToInt(Integer::parseInt).map(i -> i - 1).toArray();
            if (choices.stream().distinct().count() != choices.length) {
                prompter.show("Choose all distinct options, try again.");
                return prompter.promptValues(elements, toStringMapper, message, includeNull, tableDisplayer, sorter);
            }
            if (choices.stream().anyMatch(number -> number < 0 || number > sortedElements.size() - 1)) {
                prompter.show("The entered number(s) aren't in the range [1, %s], try again.", sortedElements.size());
                return prompter.promptValues(elements, toStringMapper, message, includeNull, tableDisplayer, sorter);
            }
            return choices.stream().map(sortedElements::get).collect(Collectors.toList());
        } catch (NumberFormatException e) {
            prompter.show("Invalid number(s) encountered. Enter a comma separated list of the choices.");
            return prompter.promptValues(elements, toStringMapper, message, includeNull, tableDisplayer, sorter);
        }
    }

    private static <T> String promptFromListIndices(Prompter prompter, Iterable<T> elements,
        @Nullable TableDisplayer<T> tableDisplayer, Function<T, String> toStringMapper,
        String message) {
        try {
            if (tableDisplayer != null) {
                tableDisplayer.display(elements);
                return prompter.prompt(message);
            } else {
                String choicesMessage = elements.stream()
                    .gather(zipWithIndex())
                    .map(entry -> "  - " + (entry.getValue() + 1) + ": " + toStringMapper.apply(entry.getKey()))
                    .collect(Collectors.joining(lineSeparator())) + lineSeparator();
                return prompter.prompt(
                    StringUtils.isBlank(message) ? choicesMessage : message + lineSeparator() + choicesMessage);
            }
        } catch (PrompterException e) {
            throw new IllegalStateException(e);
        }
    }

    private static <T> Optional<T> prompt(Prompter prompter, Function<String, T> toObjectMapper,
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
                    prompter.showMessage(
                        StringUtils.isNotBlank(errorMessage) ? errorMessage : getText("Prompter.ValueIsNotValid"));
                    return prompt(prompter, toObjectMapper, validator, objValidator, defaultValue, defaultValueSupplier,
                        message, errorMessage);
                }
                T object = toObjectMapper.apply(value);
                if (objValidator != null && !objValidator.test(object)) {
                    prompter.showMessage(
                        StringUtils.isNotBlank(errorMessage) ? errorMessage : getText("Prompter.ValueIsNotValid"));
                    return prompt(prompter, toObjectMapper, validator, objValidator, defaultValue, defaultValueSupplier,
                        message, errorMessage);
                }
                return Optional.ofNullable(object);
            }
        } catch (PrompterException e) {
            throw new IllegalStateException(e);
        }
    }

    @RequiredArgsConstructor
    public static class TableDisplayer<T> {

        private final List<ColumnDisplayer<T>> columnDisplayers;

        @SafeVarargs
        public final void display(T... tableElements) {
            String[] columnNames = columnDisplayers.stream().map(ColumnDisplayer::columnName).toArray(String[]::new);
            Object[][] dataTable = tableElements.stream()
                .map(tableElement -> columnDisplayers.stream()
                    .map(columnDisplayer -> columnDisplayer.toStringMapper().apply(tableElement))
                    .toArray())
                .toArray(Object[][]::new);

            TextTable tt = new TextTable(columnNames, dataTable);
            // this adds the numbering on the left
            tt.setAddRowNumbering(true);
            tt.printTable();
        }

        public void display(Iterable<T> tableElements) {
            String[] columnNames = columnDisplayers.stream().map(ColumnDisplayer::columnName).toArray(String[]::new);
            Object[][] dataTable = tableElements.stream()
                .map(tableElement -> columnDisplayers.stream()
                    .map(columnDisplayer -> columnDisplayer.toStringMapper().apply(tableElement))
                    .toArray())
                .toArray(Object[][]::new);

            TextTable tt = new TextTable(columnNames, dataTable);
            // this adds the numbering on the left
            tt.setAddRowNumbering(true);
            tt.printTable();
        }
    }

    public record ColumnDisplayer<T>(String columnName, Function<T, String> toStringMapper) {
    }
}
