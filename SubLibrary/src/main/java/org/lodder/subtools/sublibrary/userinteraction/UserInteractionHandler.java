package org.lodder.subtools.sublibrary.userinteraction;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Predicate;

import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.UserInteractionSettingsIntf;

public interface UserInteractionHandler {

    @val UserInteractionSettingsIntf settings;

    boolean confirm(String message, String title);

    <T> Optional<T> selectFromList(Iterable<T> options, @Nullable String message=null, @Nullable String title=null,
        @Nullable Function<T, String> toStringMapper=null);

    <T> Optional<T> choice(Iterable<T> options, @Nullable String message=null, @Nullable String title=null,
        @Nullable Function<T, String> toStringMapper=null);

    Optional<String> enter(String title, String message=null, @Nullable String errorMessage=null,
        @Nullable Predicate<String> validator=null);

    default OptionalInt enterNumber(String title, String message, String errorMessage) {
        return enter(title, message, errorMessage, StringUtils::isNumeric).mapToInt(Integer::parseInt);
    }

    void showMessage(String message, String title, MessageSeverity messageSeverity);

    enum MessageSeverity {
        INFO, WARNING, ERROR
    }
}
