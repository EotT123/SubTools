package org.lodder.subtools.sublibrary.userinteraction;

import javax.swing.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.UserInteractionSettingsIntf;
import org.lodder.subtools.sublibrary.gui.InputPane;
import org.lodder.subtools.sublibrary.util.Validator;

@AllArgsConstructor
public class UserInteractionHandlerGUI implements UserInteractionHandler {

    @val @override UserInteractionSettingsIntf settings;
    @val JFrame frame;

    @Override
    public <T> Optional<T> selectFromList(Iterable<T> options, String message,
        @Nullable String title, @Nullable Function<T, String> toStringMapper) {
        String[] optionsAsStrings = options.stream()
                .map(Objects.requireNonNullElseGet(toStringMapper, () -> String::valueOf))
                .toArray(String[]::new);
        int selection =
                JOptionPane.showOptionDialog(frame, message, title, JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, optionsAsStrings,
                        optionsAsStrings[0]);
        return selection == JOptionPane.CLOSED_OPTION ? Optional.empty() : options.stream().skip(selection).findFirst();
    }

    @Override
    public boolean confirm(String message, String title) {
        int choice = Integer.parseInt(JOptionPane.showInputDialog(frame, message, title, JOptionPane.YES_NO_OPTION));
        return choice == JOptionPane.YES_OPTION;
    }

    @Override
    public Optional<String> enter(String title, String message, @Nullable List<Validator<String>> inputValidators) {
        return new InputPane<>(
            title:title,
            message:message,
            inputValidators:inputValidators,
            toObjectMapper:Function.identity())
            .prompt();
    }

    @Override
    public OptionalInt enterNumber(String title, String message,
        @Nullable List<Validator<Integer>> objectValidators) {

        return new InputPane<>(
            title:title,
            message:message,
            toObjectMapper:Integer::parseInt,
            objectValidators:objectValidators)
            .prompt().mapToInt(v -> v);
    }

    public void message(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.OK_OPTION);
    }

    @Override
    public void showMessage(String message, String title, MessageSeverity messageSeverity) {
        int messageType = switch (messageSeverity) {
            case INFO -> JOptionPane.INFORMATION_MESSAGE;
            case WARNING -> JOptionPane.WARNING_MESSAGE;
            case ERROR -> JOptionPane.ERROR_MESSAGE;
        };
        JOptionPane.showMessageDialog(frame, message, title, messageType);
    }
}
