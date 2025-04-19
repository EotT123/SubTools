package org.lodder.subtools.sublibrary.util.prompter;

import static com.pivovarit.gatherers.MoreGatherers.*;
import static java.lang.System.*;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.jspecify.annotations.Nullable;

@Deprecated
public class PrompterValuesFromList<T> {

    private final Iterable<T> elements;
    private final Function<T, String> toStringMapper;
    private final String message;
    private final boolean includeNull;
    @Nullable private final TableDisplayer<T> tableDisplayer;
    @Nullable private final Comparator<T> comparator;

    // TODO: tableDisplayer or ToStingMapper
    public PrompterValuesFromList(
        Iterable<T> elements,
        Function<T, String> toStringMapper = String::valueOf,
        String message,
        boolean includeNull,
        @Nullable TableDisplayer<T> tableDisplayer=null,
        @Nullable Comparator<T> sort=null
    ) {
        this.elements = elements;
        this.toStringMapper = toStringMapper;
        this.message = message;
        this.includeNull = includeNull;
        this.tableDisplayer = tableDisplayer;
        this.comparator = sort;
    }

    public List<T> prompt(Prompter prompter) {
        List<T> sortedElements =
            comparator == null ? elements.stream().toList() : elements.stream().sorted(comparator).toList();
        try {
            String value;
            if (tableDisplayer != null) {
                tableDisplayer.display(sortedElements);
                value = prompter.prompt(message);
            } else {
                String choicesMessage = sortedElements.stream()
                    .gather(zipWithIndex())
                    .map(entry -> "  - " + (entry.getValue() + 1) + ": " + toStringMapper.apply(entry.getKey()))
                    .collect(Collectors.joining(lineSeparator())) + lineSeparator();
                value = prompter.prompt(
                    StringUtils.isBlank(message) ? choicesMessage : message + lineSeparator() + choicesMessage);
            }
            if (StringUtils.isBlank(value) && includeNull) {
                return List.of();
            }
            if (StringUtils.isBlank(value)) {
                prompter.show("Enter a valid value, try again.");
                return prompt(prompter);
            }
            int[] choices = value.split(",").stream().mapToInt(Integer::parseInt).map(i -> i - 1).toArray();
            if (choices.stream().distinct().count() != choices.length) {
                prompter.show("Choose all distinct options, try again.");
                return prompt(prompter);
            }
            if (choices.stream().anyMatch(number -> number < 0 || number > sortedElements.size() - 1)) {
                prompter.show("The entered number(s) aren't in the range [1, %s], try again.",                    sortedElements.size());
                return prompt(prompter);
            }
            return choices.stream().map(sortedElements::get).collect(Collectors.toList());
        } catch (PrompterException e) {
            throw new IllegalStateException(e);
        } catch (NumberFormatException e) {
            prompter.show("Invalid number(s) encountered. Enter a comma separated list of the choices.");
            return prompt(prompter);
        }
    }
}
