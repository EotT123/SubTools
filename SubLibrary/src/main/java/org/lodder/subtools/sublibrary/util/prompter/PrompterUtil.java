package org.lodder.subtools.sublibrary.util.prompter;

import java.util.function.Function;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.lodder.subtools.sublibrary.util.prompter.PrompterBuilderValueFromList.ValueFromListPromptBuilderIntf;
import org.lodder.subtools.sublibrary.util.prompter.PrompterBuilderValueFromList.ValueFromListToStringMapperBuilderIntf;
import org.lodder.subtools.sublibrary.util.prompter.PrompterBuilderValuesFromList.ValuesFromListPromptBuilderIntf;
import org.lodder.subtools.sublibrary.util.prompter.PrompterBuilderValuesFromList.ValuesFromListToStringMapperBuilderIntf;

public class PrompterUtil {

    private PrompterUtil() {
        // util class
    }

    public static Prompter showMessage(Prompter prompter, String message, Object... replacements) {
        try {
            prompter.showMessage(message.formatted(replacements));
            return prompter;
        } catch (PrompterException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void pressAnyKeyToContinue(Prompter prompter) {
        new PrompterString(message:"Press any key to continue", defaultValue:"").prompt(prompter);
    }



    // ######################### \\
    // ## Get Value From List ## \\
    // ######################### \\

    public static ValueFromListPromptBuilderIntf<String> getStringFromList(Iterable<String> elements) {
        return PrompterBuilderValueFromList.getElementFromList(elements).toStringMapper(Function.identity());
    }

    public static <T> ValueFromListToStringMapperBuilderIntf<T> getElementFromList(Iterable<T> elements) {
        return PrompterBuilderValueFromList.getElementFromList(elements);
    }

    public static <T> ValueFromListToStringMapperBuilderIntf<T> getElementFromList(T[] elements) {
        return PrompterBuilderValueFromList.getElementFromList(elements);
    }

    // ########################## \\
    // ## Get Values From List ## \\
    // ########################## \\

    public static ValuesFromListPromptBuilderIntf<String> getStringsFromList(Iterable<String> elements) {
        return PrompterBuilderValuesFromList.getStringsFromList(elements);
    }

    public static <T> ValuesFromListToStringMapperBuilderIntf<T> getElementsFromList(Iterable<T> elements) {
        return PrompterBuilderValuesFromList.getElementsFromList(elements);
    }

    public static <T> ValuesFromListToStringMapperBuilderIntf<T> getElementsFromList(T[] elements) {
        return PrompterBuilderValuesFromList.getElementsFromList(elements);
    }
}
