package org.lodder.subtools.sublibrary.util.prompter;

import static com.pivovarit.gatherers.MoreGatherers.*;
import static java.lang.System.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.jspecify.annotations.Nullable;

@Deprecated
public class PrompterValueFromList<T> {

    private final Iterable<T> elements;
    private final Function<T, String> toStringMapper;
    private final String message;
    private final boolean includeNull;
    private final T emptyValue;
    @Nullable private final TableDisplayer<T> tableDisplayer;
    @Nullable private final Comparator<T> comparator;

    public PrompterValueFromList(
        Iterable<T> elements,
        Function<T, String> toStringMapper,
        String message,
        boolean includeNull,
        T emptyValue=null,
        @Nullable TableDisplayer<T> tableDisplayer=null,
        @Nullable Comparator<T> sort=null
    ) {
        this.elements = elements;
        this.toStringMapper = toStringMapper;
        this.message = message;
        this.includeNull = includeNull;
        this.emptyValue = emptyValue;
        this.tableDisplayer = tableDisplayer;
        this.comparator = sort;
    }

    public Optional<T> prompt(Prompter prompter) {
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
                return Optional.ofNullable(emptyValue);
            }
            int number = Integer.parseInt(value);
            if (number < 1 || number > sortedElements.size()) {
                prompter.show("The entered value isn't in the range [1, %s], try again.", sortedElements.size());
                return prompt(prompter);
            }
            return Optional.ofNullable(sortedElements.get(number - 1));
        } catch (PrompterException e) {
            throw new IllegalStateException(e);
        } catch (NumberFormatException e) {
            prompter.show("Enter a valid number, try again.");
            return prompt(prompter);
        }
    }
}
