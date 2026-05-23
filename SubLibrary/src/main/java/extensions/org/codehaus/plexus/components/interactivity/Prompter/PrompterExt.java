package extensions.org.codehaus.plexus.components.interactivity.Prompter;

import static com.pivovarit.gatherers.MoreGatherers.*;
import static java.lang.System.*;
import static org.lodder.subtools.multisubdownloader.Messages.*;
import static util.Utils.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

import dnl.utils.text.table.TextTable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.Validator;

// see #692
//@Extension
@NullMarked
public class PrompterExt {

    private PrompterExt() {
        // hide utility class constructor
    }

    public static Validator<String> NON_BLANK_VALIDATOR =
        new Validator<>(StringUtils::isNotBlank, getText("Prompter.ValueNonBlank"));
    public static Validator<String> INT_VALIDATOR =
        new Validator<>(v -> v.parseAsNumber(Integer::parseInt) != null);
    public static Validator<String> BOOLEAN_VALIDATOR =
        new Validator<>(v -> getText("Prompter.YesAbbreviation").equalsIgnoreCase(v) ||
            getText("Prompter.Yes").equalsIgnoreCase(v) ||
            getText("Prompter.NoAbbreviation").equalsIgnoreCase(v) ||
            getText("Prompter.No").equalsIgnoreCase(v));

    public static void show(Prompter prompter, String message, Object... replacements) {
        try {
            prompter.showMessage(message.formatted(replacements));
        } catch (PrompterException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void pressAnyKeyToContinue(Prompter prompter) {
        prompt(prompter, "Press any key to continue", List.of(), Function.identity());
    }

    public static Optional<String> promptString(Prompter prompter, String message,
        @Nullable List<Validator<String>> inputValidators=List.of(NON_BLANK_VALIDATOR),
        @Nullable List<Validator<String>> objectValidators=new ArrayList<Validator<String>>()) {

        return prompt(prompter, message, ifNullThenGet(inputValidators, () -> List.of(NON_BLANK_VALIDATOR)),
            Function.identity(), ifNullThenGet(objectValidators, List::of));
    }

    public static OptionalInt promptInt(Prompter prompter, String message,
        @Nullable List<Validator<String>> inputValidators=List.of(NON_BLANK_VALIDATOR, INT_VALIDATOR),
        @Nullable Function<String, Integer> toObjectMapper=Integer::parseInt,
        @Nullable List<Validator<Integer>> objectValidators=new ArrayList<Validator<Integer>>()) {

        return prompt(prompter, message,
            ifNullThenGet(inputValidators, () -> List.of(NON_BLANK_VALIDATOR, INT_VALIDATOR)),
            toObjectMapper != null ? toObjectMapper : Integer::parseInt,
            ifNullThenGet(objectValidators, List::of)).mapToIntEx(v -> v);
    }

    public static Optional<Boolean> promptBoolean(Prompter prompter, String message,
        List<Validator<String>> inputValidators=List.of(NON_BLANK_VALIDATOR, BOOLEAN_VALIDATOR)) {

        return prompt(prompter, message, inputValidators, Boolean::parseBoolean, List.of());
    }

    public static <T> Optional<T> promptValueFromList(Prompter prompter,
        String message,
        Iterable<T> elements,
        Function<T, String> toStringMapper=String::valueOf,
        boolean includeNull,
        @Nullable TableDisplayer<T> tableDisplayer=null,
        @Nullable Comparator<T> sorter=null) {

        Validator<@Nullable String> inputValidator =
            new Validator<@Nullable String>(v -> v == null || v.parseAsNumber(Integer::parseUnsignedInt) != null);
        Function<@Nullable String, @Nullable Integer> toObjectMapper =
            v -> v == null ? null : Integer.parseUnsignedInt(v);
        int numberOfElements = elements.size();
        List<Validator<@Nullable Integer>> objectValidators = List.of(
            new Validator<@Nullable Integer>(number -> number == null || (number > 0 && number <= numberOfElements),
                getText("Prompter.ValueNotInRange", numberOfElements)));

        TriFunction<String, List<Validator<String>>, List<T>, Optional<T>> promptFunction = (choicesMessage,
            inputValidators, sortedElements) ->
            // TODO use extension method
            PrompterExt.promptInt(prompter, choicesMessage, inputValidators, toObjectMapper, objectValidators)
                .mapToObj(idx -> sortedElements.get(idx - 1));

        return promptFromList(message, elements, toStringMapper, includeNull, tableDisplayer, sorter,
            List.of(inputValidator), promptFunction);
    }

    public static <T> List<T> promptValuesFromList(Prompter prompter,
        String message,
        Iterable<T> elements,
        Function<T, String> toStringMapper=String::valueOf,
        boolean includeNull,
        @Nullable TableDisplayer<T> tableDisplayer=null,
        @Nullable Comparator<T> sorter=null) {

        Validator<@Nullable String> inputValidator = new Validator<@Nullable String>(v -> v == null ||
            v.split(",").stream().allMatch(n -> n.parseAsNumber(Integer::parseUnsignedInt) != null));
        Function<String, int[]> toObjectsMapper =
            v -> Arrays.stream(v.split(",")).mapToInt(Integer::parseUnsignedInt).toArray();
        int numberOfElements = elements.size();
        List<Validator<int[]>> objectValidators = List.of(
            new Validator<>(numbers -> numbers.stream().distinct().count() == numbers.stream().count(),
                getText("Prompter.DistinctValues")),
            new Validator<>(numbers -> numbers.stream().allMatch(number -> number > 0 && number <= numberOfElements),
                getText("Prompter.ValueNotInRange", numberOfElements)));

        TriFunction<String, List<Validator<String>>, List<T>, List<T>> promptFunction = (choicesMessage,
            inputValidators, sortedElements) ->
            // TODO use extension method
            PrompterExt.promptValues(prompter, choicesMessage, inputValidators, toObjectsMapper, objectValidators)
                .stream().map(idx -> sortedElements.get(idx - 1)).toList();

        return promptFromList(message, elements, toStringMapper, includeNull, tableDisplayer, sorter,
            List.of(inputValidator), promptFunction);
    }

    private static <T, R> R promptFromList(
        String message,
        Iterable<T> elements,
        Function<T, String> toStringMapper=String::valueOf,
        boolean includeNull,
        @Nullable TableDisplayer<T> tableDisplayer=null,
        @Nullable Comparator<T> sorter=null,
        List<Validator<String>> inputValidators=new ArrayList<Validator<String>>(),
        TriFunction<String, List<Validator<String>>, List<T>, R> promptFunction) {

        List<T> sortedElements =
            sorter == null ? elements.stream().toList() : elements.stream().sorted(sorter).toList();
        String choicesMessage;
        if (tableDisplayer != null) {
            choicesMessage = tableDisplayer.getAsString(sortedElements);
        } else {
            choicesMessage = message + lineSeparator() +
                sortedElements.stream()
                    .gather(zipWithIndex())
                    .map(entry -> "  - " + (entry.getValue() + 1) + ": " + toStringMapper.apply(entry.getKey()))
                    .collect(Collectors.joining(lineSeparator())) +
                lineSeparator();
        }
        List<Validator<String>> allInputValidators = new ArrayList<>();
        if (!includeNull) {
            allInputValidators.add(NON_BLANK_VALIDATOR);
        }
        allInputValidators.addAll(inputValidators);

        return promptFunction.apply(choicesMessage, allInputValidators, sortedElements);
    }

    private static <T> Optional<T> prompt(Prompter prompter, String message,
        List<Validator<String>> inputValidators=new ArrayList<Validator<String>>(),
        Function<String, @Nullable T> toObjectMapper,
        List<Validator<@Nullable T>> objectValidators=new ArrayList<Validator<T>>()) {
        try {
            String value = prompter.prompt(message + System.lineSeparator());
            for (Validator<String> inputValidator : inputValidators) {
                if (inputValidator.isInvalid(value)) {
                    prompter.showMessage(inputValidator.errorMessage);
                    return prompt(prompter, message, inputValidators, toObjectMapper, objectValidators);
                }
            }
            T object = toObjectMapper.apply(value);
            for (Validator<@Nullable T> objectValidator : objectValidators) {
                if (objectValidator.isInvalid(object)) {
                    prompter.showMessage(objectValidator.errorMessage);
                    return prompt(prompter, message, inputValidators, toObjectMapper, objectValidators);
                }
            }
            return Optional.ofNullable(object);
        } catch (PrompterException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T promptValues(Prompter prompter, String message,
        List<Validator<String>> inputValidators=new ArrayList<Validator<String>>(),
        Function<String, T> toObjectsMapper,
        List<Validator<T>> objectValidators=new ArrayList<Validator<T>>()) {
        try {
            String value = prompter.prompt(message + System.lineSeparator());
            for (Validator<String> inputValidator : inputValidators) {
                if (inputValidator.isInvalid(value)) {
                    prompter.showMessage(inputValidator.errorMessage);
                    // TODO use extension method
                    return PrompterExt.promptValues(prompter, message, inputValidators, toObjectsMapper,
                        objectValidators);
                }
            }
            T objects = toObjectsMapper.apply(value);
            for (Validator<T> objectValidator : objectValidators) {
                if (objectValidator.isInvalid(objects)) {
                    prompter.showMessage(objectValidator.errorMessage);
                    // TODO use extension method
                    return PrompterExt.promptValues(prompter, message, inputValidators, toObjectsMapper,
                        objectValidators);
                }
            }
            return objects;
        } catch (PrompterException e) {
            throw new IllegalStateException(e);
        }
    }

    @NullMarked
    public static class TableDisplayer<T> {

        private final List<ColumnDisplayer<T>> columnDisplayers;

        public TableDisplayer(List<ColumnDisplayer<T>> columnDisplayers) {
            this.columnDisplayers = columnDisplayers;
        }

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
            writeToPrintStream(tableElements, System.out);
        }

        public String getAsString(Iterable<T> tableElements) {
            LineReadingOutputStream lineReadingOutputStream = new LineReadingOutputStream();
            PrintStream printStream = new PrintStream(lineReadingOutputStream);
            writeToPrintStream(tableElements, printStream);
            return lineReadingOutputStream.toString();
        }

        private void writeToPrintStream(Iterable<T> tableElements, PrintStream printStream) {
            String[] columnNames = columnDisplayers.stream().map(ColumnDisplayer::columnName).toArray(String[]::new);
            Object[][] dataTable = tableElements.stream()
                .map(tableElement -> columnDisplayers.stream()
                    .map(columnDisplayer -> columnDisplayer.toStringMapper().apply(tableElement))
                    .toArray())
                .toArray(Object[][]::new);

            TextTable tt = new TextTable(columnNames, dataTable);
            // this adds the numbering on the left
            tt.setAddRowNumbering(true);
            tt.printTable(printStream, 0);
        }
    }

    @NullMarked
    private static class LineReadingOutputStream extends OutputStream {
        private final ByteArrayOutputStream byteArrayOutputStream;

        // Constructor
        public LineReadingOutputStream() {
            this.byteArrayOutputStream = new ByteArrayOutputStream();
        }

        // Write method, called when you write data to the stream
        @Override
        public void write(int b) throws IOException {
            byteArrayOutputStream.write(b);
        }

        public String toString() {
            return byteArrayOutputStream.toString();
        }

    }

    @NullMarked
    public record ColumnDisplayer<T>(String columnName, Function<? super T, String> toStringMapper) {
    }
}
