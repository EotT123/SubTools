package org.lodder.subtools.sublibrary.userinteraction;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.UserInteractionSettingsIntf;
import org.lodder.subtools.sublibrary.util.Validator;

@NullMarked
public interface UserInteractionHandler {

    @val UserInteractionSettingsIntf settings;

    boolean confirm(String message, String title);

    <T> Optional<T> selectFromList(Iterable<T> options, String message, @Nullable String title=null,
        Function<T, String> toStringMapper=String::valueOf);

    Optional<String> enter(String message, @Nullable String title=null,
        @Nullable List<Validator<String>> inputValidators=null);

    OptionalInt enterNumber(String title, String message, @Nullable List<Validator<Integer>> objectValidators=null);

    void showMessage(String message, String title, MessageSeverity messageSeverity);

    enum MessageSeverity {
        INFO, WARNING, ERROR
    }
}
